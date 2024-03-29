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
 * An exception to be thrown by a parser when the syntax of a file is invalid.
 */
@Deprecated
public class MalformedSyntaxException extends ParseException {
	public MalformedSyntaxException() {
		super();
	}

	public MalformedSyntaxException(String message) {
		super(message);
	}

	public MalformedSyntaxException(Throwable cause) {
		super(cause);
	}

	public MalformedSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedSyntaxException(JsonReader reader) {
		super(reader, "Malformed syntax");
	}

	public MalformedSyntaxException(JsonReader reader, String message) {
		super(reader, message);
	}

	public MalformedSyntaxException(JsonReader reader, Throwable cause) {
		super(reader, "Malformed syntax", cause);
	}

	public MalformedSyntaxException(JsonReader reader, String message, Throwable cause) {
		super(reader, message, cause);
	}
}
