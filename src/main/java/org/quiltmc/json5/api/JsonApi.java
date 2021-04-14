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

import org.quiltmc.json5.api.exception.ParseException;
import org.quiltmc.json5.api.stream.JsonStreamReader;
import org.quiltmc.json5.api.stream.JsonStreamWriter;
import org.quiltmc.json5.impl.stream.JsonStreamReaderImpl;
import org.quiltmc.json5.impl.stream.JsonStreamWriterImpl;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonApi {
	/**
	 * Parses a JSON5 file into a tree of its values.
	 * @return A {@link String}, {@link Boolean}, {@link Number}, {@code null},
	 * or a {@link java.util.LinkedHashMap}&lt;String, Object&gt; or {@link java.util.List}&lt;Object&gt; containing any of these types.
	 * @throws ParseException if the file is unable to be read or parsed
	 */
	public static Object parseToTree(Path path) throws IOException {
		return parseToTree(new String(Files.readAllBytes(path)));
	}

	/**
	 * Parses JSON5 text into a tree of its values.
	 * @return A {@link String}, {@link Boolean}, {@link Number}, {@code null},
	 * or a {@link java.util.LinkedHashMap}&lt;String, Object&gt; or {@link java.util.List}&lt;Object&gt; containing any of these types.
	 * @throws ParseException if the text is unable to be parsed
	 */
	public static Object parseToTree(String text) throws IOException {
		try (JsonStreamReader reader = streamReader(text)) {
			return parseToTree(reader);
		}
	}

	public static Object parseToTree(JsonStreamReader reader) throws IOException {
		return JsonInternal.readTree(reader);
	}

	public static JsonStreamReader streamReader(Path path) throws IOException {
		return new JsonStreamReaderImpl(Files.newBufferedReader(path));
	}

	public static JsonStreamReader streamReader(String text) throws IOException {
		return new JsonStreamReaderImpl(new StringReader(text));
	}

	// we don't wrap the IOException because JsonWriter already is covered in throws
	public static JsonStreamWriter streamWriter(Path path) throws IOException {
		return streamWriter(Files.newBufferedWriter(path));
	}

	public static JsonStreamWriter streamWriter(Writer writer) throws IOException {
		return new JsonStreamWriterImpl(writer);
	}
}
