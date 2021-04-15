/*
 * Copyright 2021 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.json5.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/*
 * This API is meant to be a nearly drop-in replacement for GSON's JsonWriter API.
 * The API methods are largely the same--the only major addition is the ability to write comments.
 * Gson is Copyright (C) 2010 Google Inc, under the Apache License Version 2.0 (the same as in the header above).
 * You may view the original, including its license header, here:
 * https://github.com/google/gson/blob/530cb7447089ccc12dc2009c17f468ddf2cd61ca/gson/src/main/java/com/google/gson/stream/JsonWriter.java
 */

/**
 * Writes a <a href="https://json5.org/"> JSON5</a> or strict JSON (<a href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>)
 * encoded value to a stream, one token at a time. The stream includes both
 * literal values (strings, numbers, booleans and nulls) as well as the begin
 * and end delimiters of objects and arrays.
 *
 * <h3>Encoding JSON</h3>
 * To encode your data as JSON, create a new {@code JsonWriter}. Each JSON
 * document must contain one top-level array or object. Call methods on the
 * writer as you walk the structure's contents, nesting arrays and objects as
 * necessary:
 * <ul>
 *   <li>To write <strong>arrays</strong>, first call {@link #beginArray()}.
 *       Write each of the array's elements with the appropriate {@link #value}
 *       methods or by nesting other arrays and objects. Finally close the array
 *       using {@link #endArray()}.
 *   <li>To write <strong>objects</strong>, first call {@link #beginObject()}.
 *       Write each of the object's properties by alternating calls to
 *       {@link #name} with the property's value. Write property values with the
 *       appropriate {@link #value} method or by nesting other objects or arrays.
 *       Finally close the object using {@link #endObject()}.
 * </ul>
 *
 * <h3>Example</h3>
 * Suppose we'd like to encode a stream of messages such as the following: <pre> {@code
 * [
 *   {
 *     id: 912345678901,
 *     text: "How do I stream JSON in Java?",
 *     geo: null,
 *     user: {
 *       name: "json_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     id: 912345678902,
 *     text: "@json_newb just use JsonWriter!",
 *     geo: [50.454722, -104.606667],
 *     user: {
 *       name: "jesse",
 *       followers_count: 2
 *     }
 *   }
 * ]}</pre>
 * This code encodes the above structure: <pre>   {@code
 *   public void writeJsonStream(OutputStream out, List<Message> messages) throws IOException {
 *     JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
 *     writer.setIndent("    ");
 *     writeMessagesArray(writer, messages);
 *     writer.close();
 *   }
 *
 *   public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
 *     writer.beginArray();
 *     for (Message message : messages) {
 *       writeMessage(writer, message);
 *     }
 *     writer.endArray();
 *   }
 *
 *   public void writeMessage(JsonWriter writer, Message message) throws IOException {
 *     writer.beginObject();
 *     writer.name("id").value(message.getId());
 *     writer.name("text").value(message.getText());
 *     if (message.getGeo() != null) {
 *       writer.name("geo");
 *       writeDoublesArray(writer, message.getGeo());
 *     } else {
 *       writer.name("geo").nullValue();
 *     }
 *     writer.name("user");
 *     writeUser(writer, message.getUser());
 *     writer.endObject();
 *   }
 *
 *   public void writeUser(JsonWriter writer, User user) throws IOException {
 *     writer.beginObject();
 *     writer.name("name").value(user.getName());
 *     writer.name("followers_count").value(user.getFollowersCount());
 *     writer.endObject();
 *   }
 *
 *   public void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
 *     writer.beginArray();
 *     for (Double value : doubles) {
 *       writer.value(value);
 *     }
 *     writer.endArray();
 *   }}</pre>
 *
 * <p>Each {@code JsonWriter} may be used to write a single JSON stream.
 * Instances of this class are not thread safe. Calls that would result in a
 * malformed JSON string will fail with an {@link IllegalStateException}.
 */

@ApiStatus.NonExtendable
public interface JsonWriter extends AutoCloseable, Flushable {
	// TODO: Convert these static methods to constructors
	static JsonWriter writer(Path path) throws IOException {
		Objects.requireNonNull(path, "Path cannot be null");

		return writer(Files.newBufferedWriter(path));
	}

	static JsonWriter writer(Writer writer) {
		Objects.requireNonNull(writer, "Writer cannot be null");

		return new JsonWriterImpl(writer);
	}

	/**
	 * Sets the indentation string to be repeated for each level of indentation
	 * in the encoded document. If {@code indent.isEmpty()} the encoded document
	 * will be compact. Otherwise the encoded document will be more
	 * human-readable. This defaults to a tab character.
	 *
	 * @param indent a string containing only whitespace.
	 */
	void setIndent(String indent);

	/**
	 * Configure if the output must be strict JSON, instead of strict JSON5. This flag disables NaN, (+/-)Infinity, quotes around keys, and comments.
	 * The default is false.
	 */
	void setStrict(boolean strict);

	/**
	 * Returns true if the output must be strict JSON, instead of strict JSON5. The default is false.
	 */
	boolean isStrict();

	/**
	 * Shortcut for {@code setIndent("")} that makes the encoded document significantly more compact.
	 */
	void setCompact();

	/**
	 * Returns true if the output will be compact (entirely one line) and false if it will be human-readable with newlines and indentation.
	 * The default is false.
	 */
	boolean isCompact();

	/**
	 * Configure this writer to emit JSON that's safe for direct inclusion in HTML
	 * and XML documents. This escapes the HTML characters {@code <}, {@code >},
	 * {@code &} and {@code =} before writing them to the stream. Without this
	 * setting, your XML/HTML encoder should replace these characters with the
	 * corresponding escape sequences.
	 */
	void setHtmlSafe(boolean htmlSafe);
	/**
	 * Returns true if this writer writes JSON that's safe for inclusion in HTML
	 * and XML documents.
	 */
	boolean isHtmlSafe();

	/**
	 * Sets whether object members are serialized when their value is null.
	 * This has no impact on array elements. The default is true.
	 */
	void setSerializeNulls(boolean serializeNulls);

	/**
	 * Returns true if object members are serialized when their value is null.
	 * This has no impact on array elements. The default is true.
	 */
	boolean getSerializeNulls();

	/**
	 * Begins encoding a new array. Each call to this method must be paired with
	 * a call to {@link #endArray}.
	 *
	 * @return this writer.
	 */
	public JsonWriter beginArray() throws IOException;

	/**
	 * Ends encoding the current array.
	 *
	 * @return this writer.
	 */
	public JsonWriter endArray() throws IOException;

	/**
	 * Begins encoding a new object. Each call to this method must be paired
	 * with a call to {@link #endObject}.
	 *
	 * @return this writer.
	 */
	public JsonWriter beginObject() throws IOException;

	/**
	 * Ends encoding the current object.
	 *
	 * @return this writer.
	 */
	public JsonWriter endObject() throws IOException;

	/**
	 * Encodes the property name.
	 *
	 * @param name the name of the forthcoming value. May not be null.
	 * @return this writer.
	 */
	JsonWriter name(String name) throws IOException;

	/**
	 * Encodes a comment, handling newlines and HTML safety gracefully. Silently does nothing when strict JSON mode is enabled.
	 * @param comment the comment to write, or null to encode nothing.
	 */
	JsonWriter comment(@Nullable String comment) throws IOException;
	JsonWriter blockComment(@Nullable String comment) throws IOException;
	/**
	 * Encodes {@code value}.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public JsonWriter value(String value) throws IOException;

	/**
	 * Writes {@code value} directly to the writer without quoting or
	 * escaping.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public JsonWriter jsonValue(String value) throws IOException;

	/**
	 * Encodes {@code null}.
	 *
	 * @return this writer.
	 */
	public JsonWriter nullValue() throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonWriter value(boolean value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonWriter value(Boolean value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities}.
	 * @return this writer.
	 */
	public JsonWriter value(double value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonWriter value(long value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities} if in strict mode.
	 * @return this writer.
	 */
	public JsonWriter value(Number value) throws IOException;

	/**
	 * Ensures all buffered data is written to the underlying {@link Writer}
	 * and flushes that writer.
	 */
	public void flush() throws IOException;

	/**
	 * Flushes and closes this writer and the underlying {@link Writer}.
	 *
	 * @throws IOException if the JSON document is incomplete.
	 */
	public void close() throws IOException;
}
