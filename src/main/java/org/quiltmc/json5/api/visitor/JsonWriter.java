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

package org.quiltmc.json5.api.visitor;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

// Technically, the writing visitors could extend the normal JsonVisitors, but making them different makes the implementation and usage
// of the API much nicer.
public interface JsonWriter extends Flushable, Closeable {
	JsonWriter comment(String comment) throws IOException;
	JsonWriter blockComment(String comment) throws IOException;

	default JsonObjectWriter<JsonWriter> writeObject() throws IOException {
		return writeObject(null);
	}

	default JsonArrayWriter<JsonWriter> writeArray() throws IOException {
		return writeArray(null);
	}

	default JsonWriter write(String string) throws IOException {
		return write(string, null);
	}

	default JsonWriter write(Number number) throws IOException {
		return write(number, null);
	}

	default JsonWriter write(boolean bool) throws IOException {
		return write(bool, null);
	}

	default JsonWriter writeNull() throws IOException {
		return writeNull(null);
	}
	
	JsonObjectWriter<JsonWriter> writeObject(@Nullable String comment) throws IOException;
	JsonArrayWriter<JsonWriter> writeArray(@Nullable String comment) throws IOException;
	JsonWriter write(String string, @Nullable String comment) throws IOException;
	JsonWriter write(boolean bool, @Nullable String comment) throws IOException;
	JsonWriter write(Number number, @Nullable String comment) throws IOException;
	JsonWriter writeNull(@Nullable String comment) throws IOException;
}
