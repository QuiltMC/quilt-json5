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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.stream.JsonStreamWriter;
import org.quiltmc.json5.api.visitor.writer.JsonArrayWriter;
import org.quiltmc.json5.api.visitor.writer.JsonObjectWriter;
import org.quiltmc.json5.api.visitor.writer.JsonWriter;

import java.io.IOException;

@ApiStatus.Internal
public final class JsonWriterImpl implements JsonWriter {
	private final JsonStreamWriter writer;

	public JsonWriterImpl(JsonStreamWriter writer) {
		this.writer = writer;
	}

	@Override
	public JsonWriter comment(String comment) throws IOException {
		writer.comment(comment);
		return this;
	}

	@Override
	public JsonWriter blockComment(String comment) throws IOException {
		writer.blockComment(comment);
		return this;
	}

	@Override
	public JsonObjectWriter<JsonWriter> writeObject(@Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.beginObject();
		return new JsonObjectWriterImpl<>(writer, this);
	}

	@Override
	public JsonArrayWriter<JsonWriter> writeArray(@Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.beginArray();
		return new JsonArrayWriterImpl<>(writer, this);
	}

	@Override
	public JsonWriter write(String string, @Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.value(string);
		return this;
	}

	@Override
	public JsonWriter write(boolean bool, @Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.value(bool);
		return this;
	}

	@Override
	public JsonWriter write(Number number, @Nullable String comment) throws IOException {
		writer.comment(comment);
		return this;
	}

	@Override
	public JsonWriter writeNull(@Nullable String comment) throws IOException {
		writer.comment(comment);
		writer.nullValue();
		return this;
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}
}
