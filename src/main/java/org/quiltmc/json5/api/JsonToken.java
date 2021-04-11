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
import org.quiltmc.json5.api.stream.JsonStreamReader;
import org.quiltmc.json5.api.stream.JsonStreamWriter;

/*
 * This is a carbon-copy of Gson's JsonToken.
 * Gson is Copyright (C) 2010 Google Inc, under the Apache License Version 2.0 (the same as in the header above).
 * You may view the original, including its license header, here:
 * https://github.com/google/gson/blob/530cb7447089ccc12dc2009c17f468ddf2cd61ca/gson/src/main/java/com/google/gson/stream/JsonToken.java
 */
/**
 * A structure, name or value type in a JSON-encoded string.
 *
 * @author Jesse Wilson
 * @since 1.6
 */
public enum JsonToken {

	/**
	 * The opening of a JSON array. Written using {@link JsonStreamWriter#beginArray}
	 * and read using {@link JsonStreamReader#beginArray}.
	 */
	BEGIN_ARRAY,

	/**
	 * The closing of a JSON array. Written using {@link JsonStreamWriter#endArray}
	 * and read using {@link JsonStreamReader#endArray}.
	 */
	END_ARRAY,

	/**
	 * The opening of a JSON object. Written using {@link JsonStreamWriter#beginObject}
	 * and read using {@link JsonStreamReader#beginObject}.
	 */
	BEGIN_OBJECT,

	/**
	 * The closing of a JSON object. Written using {@link JsonStreamWriter#endObject}
	 * and read using {@link JsonStreamReader#endObject}.
	 */
	END_OBJECT,

	/**
	 * A JSON property name. Within objects, tokens alternate between names and
	 * their values. Written using {@link JsonStreamWriter#name} and read using {@link
	 * JsonStreamReader#nextName}
	 */
	NAME,

	/**
	 * A JSON string.
	 */
	STRING,

	/**
	 * A JSON number represented in this API by a Java {@code double}, {@code
	 * long}, or {@code int}.
	 */
	NUMBER,

	/**
	 * A JSON {@code true} or {@code false}.
	 */
	BOOLEAN,

	/**
	 * A JSON {@code null}.
	 */
	NULL,

	/**
	 * The end of the JSON stream. This sentinel value is returned by {@link
	 * JsonStreamReader#peek()} to signal that the JSON-encoded value has no more
	 * tokens.
	 */
	END_DOCUMENT
}