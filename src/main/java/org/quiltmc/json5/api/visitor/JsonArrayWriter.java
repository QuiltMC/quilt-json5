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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@ApiStatus.NonExtendable
public interface JsonArrayWriter<T> {
	JsonArrayWriter<T> comment(String comment) throws IOException;
	JsonArrayWriter<T> blockComment(String comment) throws IOException;

	default JsonObjectWriter<JsonArrayWriter<T>> writeObject() throws IOException {
		return writeObject(null);
	}

	default JsonArrayWriter<JsonArrayWriter<T>> writeArray() throws IOException {
		return writeArray(null);
	}

	default JsonArrayWriter<T> write(String string) throws IOException {
		return write(string, null);
	}

	default JsonArrayWriter<T> write(Number number) throws IOException {
		return write(number, null);
	}

	default JsonArrayWriter<T> write(boolean bool) throws IOException {
		return write(bool, null);
	}

	default JsonArrayWriter<T> writeNull() throws IOException {
		return writeNull(null);
	}

	JsonObjectWriter<JsonArrayWriter<T>> writeObject(@Nullable String comment) throws IOException;
	JsonArrayWriter<JsonArrayWriter<T>> writeArray(@Nullable String comment) throws IOException;
	JsonArrayWriter<T> write(String string, @Nullable String comment) throws IOException;
	JsonArrayWriter<T> write(boolean bool, @Nullable String comment) throws IOException;
	JsonArrayWriter<T> write(Number number, @Nullable String comment) throws IOException;
	JsonArrayWriter<T> writeNull(@Nullable String comment) throws IOException;

	T pop() throws IOException;
}
