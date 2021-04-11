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

package org.quiltmc.json5.impl.visitor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.stream.JsonStreamWriter;
import org.quiltmc.json5.api.visitor.writer.JsonArrayWriter;
import org.quiltmc.json5.api.visitor.writer.JsonObjectWriter;

import java.io.IOException;

final class JsonArrayWriterImpl<T> implements JsonArrayWriter<T> {
	private final JsonStreamWriter writer;
	private final T parent;

	public JsonArrayWriterImpl(JsonStreamWriter writer, T parent) {
		this.writer = writer;
		this.parent = parent;
	}

	@Override
	public JsonArrayWriter<T> comment(String comment) throws IOException {
		writer.comment(comment);
		return this;
	}

	@Override
	public JsonArrayWriter<T> blockComment(String comment) throws IOException {
		writer.blockComment(comment);
		return this;
	}

	@Override
	public @NotNull JsonObjectWriter<JsonArrayWriter<T>> writeObject(@Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.beginObject();
		return new JsonObjectWriterImpl<>(writer, this);
	}

	@Override
	public @NotNull JsonArrayWriter<JsonArrayWriter<T>> writeArray(@Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.beginArray();
		return new JsonArrayWriterImpl<>(writer, this);
	}

	@Override
	public JsonArrayWriter<T> write(String string, @Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.value(string);
		return this;
	}

	@Override
	public JsonArrayWriter<T> write(boolean bool, @Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.value(bool);
		return this;
	}

	@Override
	public JsonArrayWriter<T> write(Number number, @Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.value(number);
		return this;
	}

	@Override
	public JsonArrayWriter<T> writeNull(@Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.nullValue();
		return this;
	}

	@Override
	public T pop() throws IOException {
		writer.endArray();
		return parent;
	}
}
