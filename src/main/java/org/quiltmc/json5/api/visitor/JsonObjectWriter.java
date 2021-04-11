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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@ApiStatus.NonExtendable
public interface JsonObjectWriter<T> {
	JsonObjectWriter<T> comment(String comment) throws IOException;
	JsonObjectWriter<T> blockComment(String comment) throws IOException;
	default JsonObjectWriter<JsonObjectWriter<T>> writeObject(String name) throws IOException {
		return writeObject(name, null);
	}

	default JsonArrayWriter<JsonObjectWriter<T>> writeArray(String name) throws IOException {
		return writeArray(name, null);
	}

	default JsonObjectWriter<T> write(String name, String string) throws IOException {
		return write(name, string, null);
	}

	default JsonObjectWriter<T> write(String name, Number number) throws IOException {
		return write(name, number, null);
	}

	default JsonObjectWriter<T> write(String name, boolean bool) throws IOException {
		return write(name, bool, null);
	}

	default JsonObjectWriter<T> writeNull(String name) throws IOException {
		return writeNull(name, null);
	}

	JsonObjectWriter<JsonObjectWriter<T>> writeObject(String name, @Nullable String comment) throws IOException;
	JsonArrayWriter<JsonObjectWriter<T>> writeArray(String name, @Nullable String comment) throws IOException;
	JsonObjectWriter<T> write(String name, String string, @Nullable String comment) throws IOException;
	JsonObjectWriter<T> write(String name, boolean bool, @Nullable String comment) throws IOException;
	JsonObjectWriter<T> write(String name, Number number, @Nullable String comment) throws IOException;
	JsonObjectWriter<T> writeNull(String name, @Nullable String comment) throws IOException;
	T pop() throws IOException;
}
