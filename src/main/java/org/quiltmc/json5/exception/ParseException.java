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

package org.quiltmc.json5.exception;

import org.quiltmc.json5.JsonReader;

/**
 * A generic exception relating to an error in parsing, from loading the text to be read to emitting it's final representation.
 */
@Deprecated
public class ParseException extends RuntimeException {
	public ParseException() {
		super();
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(JsonReader reader) {
		this(reader, "Parse exception");
	}

	public ParseException(JsonReader reader, String message) {
		super(String.format("%s %s", message, reader.path()));
	}

	public ParseException(JsonReader reader, Throwable cause) {
		this(reader, "Parse exception", cause);
	}

	public ParseException(JsonReader reader, String message, Throwable cause) {
		super(String.format("%s %s", message, reader.path()), cause);
	}
}
