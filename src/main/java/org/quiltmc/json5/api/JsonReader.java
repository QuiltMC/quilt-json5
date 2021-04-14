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
import org.quiltmc.json5.api.JsonToken;
import org.quiltmc.json5.impl.stream.JsonReaderImpl;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/*
 * This API is meant to be a drop-in replacement for GSON's JsonReader API.
 * The lenient mode has been replaced with a json5 mode, which only parses strict json5.
 * Gson is Copyright (C) 2010 Google Inc, under the Apache License Version 2.0 (the same as in the header above).
 * You may view the original, including its license header, here:
 * https://github.com/google/gson/blob/530cb7447089ccc12dc2009c17f468ddf2cd61ca/gson/src/main/java/com/google/gson/stream/JsonReader.java
 */

/**
 * Reads <a href="https://json5.org/"> JSON5</a> or strict JSON (<a href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>)
 * encoded value as a stream of tokens. This stream includes both literal
 * values (strings, numbers, booleans, and nulls) as well as the begin and
 * end delimiters of objects and arrays. The tokens are traversed in
 * depth-first order, the same order that they appear in the JSON document.
 * Within JSON objects, name/value pairs are represented by a single token.
 *
 * <h3>Parsing JSON</h3>
 * To create a recursive descent parser for your own JSON streams, first create
 * an entry point method that creates a {@code JsonReader}.
 *
 * <p>Next, create handler methods for each structure in your JSON text. You'll
 * need a method for each object type and for each array type.
 * <ul>
 *   <li>Within <strong>array handling</strong> methods, first call {@link
 *       #beginArray} to consume the array's opening bracket. Then create a
 *       while loop that accumulates values, terminating when {@link #hasNext}
 *       is false. Finally, read the array's closing bracket by calling {@link
 *       #endArray}.
 *   <li>Within <strong>object handling</strong> methods, first call {@link
 *       #beginObject} to consume the object's opening brace. Then create a
 *       while loop that assigns values to local variables based on their name.
 *       This loop should terminate when {@link #hasNext} is false. Finally,
 *       read the object's closing brace by calling {@link #endObject}.
 * </ul>
 * <p>When a nested object or array is encountered, delegate to the
 * corresponding handler method.
 *
 * <p>When an unknown name is encountered, strict parsers should fail with an
 * exception. Lenient parsers should call {@link #skipValue()} to recursively
 * skip the value's nested tokens, which may otherwise conflict.
 *
 * <p>If a value may be null, you should first check using {@link #peek()}.
 * Null literals can be consumed using either {@link #nextNull()} or {@link
 * #skipValue()}.
 *
 * <h3>Example</h3>
 * Suppose we'd like to parse a stream of messages such as the following: <pre> {@code
 * [
 *   {
 *     id: 912345678901,
 *     text: "How do I read a JSON stream in Java?",
 *     geo: null,
 *     user: {
 *       "name": "json_newb",
 *       followers_count: 41,
 *      }
 *   },
 *   {
 *   /*
 *    * Look mom, block comments!
 *   *\/
 *     "id": 912345678902,
 *     "text": "@json_newb just use JsonReader!",
 *     "geo": [-Infinity, NaN], // wow, broken floating point types!
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]}</pre>
 * This code implements the parser for the above structure: <pre>   {@code
 *
 *   public List<Message> readJsonStream(InputStream in) throws IOException {
 *     JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
 *     try {
 *       return readMessagesArray(reader);
 *     } finally {
 *       reader.close();
 *     }
 *   }
 *
 *   public List<Message> readMessagesArray(JsonReader reader) throws IOException {
 *     List<Message> messages = new ArrayList<Message>();
 *
 *     reader.beginArray();
 *     while (reader.hasNext()) {
 *       messages.add(readMessage(reader));
 *     }
 *     reader.endArray();
 *     return messages;
 *   }
 *
 *   public Message readMessage(JsonReader reader) throws IOException {
 *     long id = -1;
 *     String text = null;
 *     User user = null;
 *     List<Double> geo = null;
 *
 *     reader.beginObject();
 *     while (reader.hasNext()) {
 *       String name = reader.nextName();
 *       if (name.equals("id")) {
 *         id = reader.nextLong();
 *       } else if (name.equals("text")) {
 *         text = reader.nextString();
 *       } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {
 *         geo = readDoublesArray(reader);
 *       } else if (name.equals("user")) {
 *         user = readUser(reader);
 *       } else {
 *         reader.skipValue();
 *       }
 *     }
 *     reader.endObject();
 *     return new Message(id, text, user, geo);
 *   }
 *
 *   public List<Double> readDoublesArray(JsonReader reader) throws IOException {
 *     List<Double> doubles = new ArrayList<Double>();
 *
 *     reader.beginArray();
 *     while (reader.hasNext()) {
 *       doubles.add(reader.nextDouble());
 *     }
 *     reader.endArray();
 *     return doubles;
 *   }
 *
 *   public User readUser(JsonReader reader) throws IOException {
 *     String username = null;
 *     int followersCount = -1;
 *
 *     reader.beginObject();
 *     while (reader.hasNext()) {
 *       String name = reader.nextName();
 *       if (name.equals("name")) {
 *         username = reader.nextString();
 *       } else if (name.equals("followers_count")) {
 *         followersCount = reader.nextInt();
 *       } else {
 *         reader.skipValue();
 *       }
 *     }
 *     reader.endObject();
 *     return new User(username, followersCount);
 *   }}</pre>
 *
 * <h3>Number Handling</h3>
 * This reader permits numeric values to be read as strings and string values to
 * be read as numbers. For example, both elements of the JSON array {@code
 * [1, "1"]} may be read using either {@link #nextInt} or {@link #nextString}.
 * This behavior is intended to prevent lossy numeric conversions: double is
 * JavaScript's only numeric type and very large values like {@code
 * 9007199254740993} cannot be represented exactly on that platform. To minimize
 * precision loss, extremely large values should be written and read as strings
 * in JSON.
 *
 * <a id="nonexecuteprefix"/><h3>Non-Execute Prefix</h3>
 * Web servers that serve private data using JSON may be vulnerable to <a
 * href="http://en.wikipedia.org/wiki/JSON#Cross-site_request_forgery">Cross-site
 * request forgery</a> attacks. In such an attack, a malicious site gains access
 * to a private JSON file by executing it with an HTML {@code <script>} tag.
 *
 * <p>Prefixing JSON files with <code>")]}'\n"</code> makes them non-executable
 * by {@code <script>} tags, disarming the attack. Since the prefix is malformed
 * JSON, strict parsing fails when it is encountered. This class permits the
 * non-execute prefix unless {@link #disallowNonExecutePrefix()} is
 * called before parsing occurs.
 *
 * <p>Each {@code JsonStreamReader} may be used to read a single JSON stream. Instances
 * of this class are not thread safe.
 */
@ApiStatus.NonExtendable
public interface JsonReader extends Closeable {
	// TODO: Convert these static methods to constructors
	static JsonReader reader(Path path) throws IOException {
		Objects.requireNonNull(path, "Path cannot be null");

		return reader(Files.newBufferedReader(path));
	}

	static JsonReader reader(String text) {
		Objects.requireNonNull(text, "Text cannot be null");

		return reader(new StringReader(text));
	}

	static JsonReader reader(Reader reader) {
		Objects.requireNonNull(reader, "Reader cannot be null");

		return new JsonReaderImpl(reader);
	}

	/**
	 * Disables JSON5-specific features. This includes, but is not limited to: comments, lack of quotes around object keys,
	 * trailing commas, hexadecimal numbers, and enhanced floating point numbers.
	 */
	void setStrictJson();

	/**
	 * Returns true if this Reader will parse JSON strictly. Defaults to false.
	 */
	boolean isStrictJson();

	/**
	 * Web servers that serve private data using JSON may be vulnerable to <a
	 * href="http://en.wikipedia.org/wiki/JSON#Cross-site_request_forgery">Cross-site
	 * request forgery</a> attacks. In such an attack, a malicious site gains access
	 * to a private JSON file by executing it with an HTML {@code <script>} tag.
	 *
	 * <p>Prefixing JSON files with <code>")]}'\n"</code> makes them non-executable
	 * by {@code <script>} tags, disarming the attack. Since the prefix is malformed
	 * JSON, strict parsing fails when it is encountered. This class permits the
	 * non-execute prefix unless {@link #disallowNonExecutePrefix()} is
	 * called before parsing occurs.
	 */
	void disallowNonExecutePrefix();

	/**
	 * Returns true if a non-execute prefix will be ignored at the beginning of this file. Defaults to true.
	 */
	boolean allowsNonExecutePrefix();
	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * beginning of a new array.
	 */
	void beginArray() throws IOException;
	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * end of the current array.
	 */
	void endArray() throws IOException;
	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * beginning of a new object.
	 */
	void beginObject() throws IOException;
	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * end of the current object.
	 */
	void endObject() throws IOException;
	/**
	 * Returns true if the current array or object has another element.
	 */
	boolean hasNext() throws IOException;
	/**
	 * Returns the type of the next token without consuming it.
	 */
	JsonToken peek() throws IOException;
	/**
	 * Returns the next token, a {@link JsonToken#NAME property name}, and
	 * consumes it.
	 *
	 * @throws java.io.IOException if the next token in the stream is not a property
	 *     name.
	 */
	String nextName() throws IOException;
	/**
	 * Returns the {@link JsonToken#STRING string} value of the next token,
	 * consuming it. If the next token is a number, this method will return its
	 * string form.
	 *
	 * @throws IllegalStateException if the next token is not a string or if
	 *     this reader is closed.
	 */
	String nextString() throws IOException;
	/**
	 * Returns the {@link JsonToken#BOOLEAN boolean} value of the next token,
	 * consuming it.
	 *
	 * @throws IllegalStateException if the next token is not a boolean or if
	 *     this reader is closed.
	 */
	boolean nextBoolean() throws IOException;
	void nextNull() throws IOException;
	/**
	 * Returns the {@link JsonToken#NUMBER double} value of the next token,
	 * consuming it. If the next token is a string, this method will attempt to
	 * parse it as a double using {@link Double#parseDouble(String)}.
	 *
	 * @throws IllegalStateException if the next token is not a literal value.
	 * @throws NumberFormatException if the next literal value cannot be parsed
	 *     as a double, or is non-finite.
	 */
	double nextDouble() throws IOException;
	/**
	 * Returns the {@link JsonToken#NUMBER long} value of the next token,
	 * consuming it. If the next token is a string, this method will attempt to
	 * parse it as a long. If the next token's numeric value cannot be exactly
	 * represented by a Java {@code long}, this method throws.
	 *
	 * @throws IllegalStateException if the next token is not a literal value.
	 * @throws NumberFormatException if the next literal value cannot be parsed
	 *     as a number, or exactly represented as a long.
	 */
	long nextLong() throws IOException;
	/**
	 * Returns the {@link JsonToken#NUMBER int} value of the next token,
	 * consuming it. If the next token is a string, this method will attempt to
	 * parse it as an int. If the next token's numeric value cannot be exactly
	 * represented by a Java {@code int}, this method throws.
	 *
	 * @throws IllegalStateException if the next token is not a literal value.
	 * @throws NumberFormatException if the next literal value cannot be parsed
	 *     as a number, or exactly represented as an int.
	 */
	int nextInt() throws IOException;
	/**
	 * Returns the {@link JsonToken#NUMBER long} value of the next token,
	 * consuming it. If the next token is a string, this method will attempt to
	 * parse it as a long. If the next token's numeric value cannot be exactly
	 * represented by a Java {@code long}, this method throws.
	 *
	 * @throws IllegalStateException if the next token is not a literal value.
	 * @throws NumberFormatException if the next literal value cannot be parsed
	 *     as a number, or exactly represented as a long.
	 */
	Number nextNumber() throws IOException;
	/**
	 * Closes this JSON reader and the underlying {@link java.io.Reader}.
	 */
	void close() throws IOException;
	/**
	 * Skips the next value recursively. If it is an object or array, all nested
	 * elements are skipped. This method is intended for use when the JSON token
	 * stream contains unrecognized or unhandled values.
	 */
	void skipValue() throws IOException;

	/**
	 * @return a <a href="http://goessner.net/articles/JsonPath/">JsonPath</a> to the current location in the input JSON.
	 */
	String path();
}
