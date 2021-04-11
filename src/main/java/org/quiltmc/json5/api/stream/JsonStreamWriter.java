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

package org.quiltmc.json5.api.stream;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.Flushable;
import java.io.IOException;

/*
 * This API is meant to be a nearly drop-in replacement for GSON's JsonWriter API.
 * The API methods are largely the same--the only major addition is the ability to write comments.
 * Gson is Copyright (C) 2010 Google Inc, under the Apache License Version 2.0 (the same as in the header above).
 * You may view the original, including its license header, here:
 * https://github.com/google/gson/blob/530cb7447089ccc12dc2009c17f468ddf2cd61ca/gson/src/main/java/com/google/gson/stream/JsonWriter.java
 */
@ApiStatus.NonExtendable
public interface JsonStreamWriter extends AutoCloseable, Flushable {
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
	public JsonStreamWriter beginArray() throws IOException;

	/**
	 * Ends encoding the current array.
	 *
	 * @return this writer.
	 */
	public JsonStreamWriter endArray() throws IOException;

	/**
	 * Begins encoding a new object. Each call to this method must be paired
	 * with a call to {@link #endObject}.
	 *
	 * @return this writer.
	 */
	public JsonStreamWriter beginObject() throws IOException;

	/**
	 * Ends encoding the current object.
	 *
	 * @return this writer.
	 */
	public JsonStreamWriter endObject() throws IOException;

	/**
	 * Encodes the property name.
	 *
	 * @param name the name of the forthcoming value. May not be null.
	 * @return this writer.
	 */
	JsonStreamWriter name(String name) throws IOException;

	/**
	 * Encodes a comment, handling newlines and HTML safety gracefully. Silently does nothing when strict JSON mode is enabled.
	 * @param comment the comment to write, or null to encode nothing.
	 */
	JsonStreamWriter comment(@Nullable String comment) throws IOException;
	JsonStreamWriter blockComment(@Nullable String comment) throws IOException;
	/**
	 * Encodes {@code value}.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public JsonStreamWriter value(String value) throws IOException;

	/**
	 * Writes {@code value} directly to the writer without quoting or
	 * escaping.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public JsonStreamWriter jsonValue(String value) throws IOException;

	/**
	 * Encodes {@code null}.
	 *
	 * @return this writer.
	 */
	public JsonStreamWriter nullValue() throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonStreamWriter value(boolean value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonStreamWriter value(Boolean value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities}.
	 * @return this writer.
	 */
	public JsonStreamWriter value(double value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonStreamWriter value(long value) throws IOException;

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities} if in strict mode.
	 * @return this writer.
	 */
	public JsonStreamWriter value(Number value) throws IOException;

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
