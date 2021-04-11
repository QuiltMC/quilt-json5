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

import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.stream.JsonStreamWriter;
import org.quiltmc.json5.api.visitor.JsonArrayWriter;
import org.quiltmc.json5.api.visitor.JsonObjectWriter;
import org.quiltmc.json5.api.visitor.JsonWriter;

import java.io.IOException;

final class JsonObjectWriterImpl<T> implements JsonObjectWriter<T> {
	private final JsonStreamWriter writer;
	private final T parent;

	public JsonObjectWriterImpl(JsonStreamWriter writer, T parent) {
		this.writer = writer;
		this.parent = parent;
	}

	@Override
	public JsonObjectWriter<T> comment(String comment) throws IOException {
		writer.comment(comment);
		return this;
	}

	@Override
	public JsonObjectWriter<T> blockComment(String comment) throws IOException {
		writer.blockComment(comment);
		return this;
	}

	@Override
	public JsonObjectWriter<JsonObjectWriter<T>> writeObject(String name, @Nullable String comment) throws IOException {
		writer.name(name);
		writer.comment(comment);
		writer.beginObject();

		return new JsonObjectWriterImpl<>(writer, this);
	}

	@Override
	public JsonArrayWriter<JsonObjectWriter<T>> writeArray(String name, @Nullable String comment) throws IOException {
		writer.name(name);
		writer.comment(comment);
		writer.beginArray();

		return new JsonArrayWriterImpl<>(writer, this);
	}

	@Override
	public JsonObjectWriter<T> write(String name, String string, @Nullable String comment) throws IOException {
		writer.name(name);
		writer.comment(comment);
		writer.value(string);
		return this;
	}

	@Override
	public JsonObjectWriter<T> write(String name, boolean bool, @Nullable String comment) throws IOException {
		writer.name(name);
		writer.comment(comment);
		writer.value(bool);
		return this;
	}

	@Override
	public JsonObjectWriter<T> write(String name, Number number, @Nullable String comment) throws IOException {
		writer.name(name);
		writer.comment(comment);
		writer.value(number);
		return this;
	}

	@Override
	public JsonObjectWriter<T> writeNull(String name, @Nullable String comment) throws IOException {
		writer.name(name);
		writer.comment(comment);
		writer.nullValue();
		return this;
	}

	@Override
	public T pop() throws IOException {
		writer.endObject();
		return parent;
	}
}
