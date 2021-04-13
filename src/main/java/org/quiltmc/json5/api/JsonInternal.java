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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.exception.MalformedSyntaxException;
import org.quiltmc.json5.api.stream.JsonStreamReader;

/**
 * Utility class to hold methods considered as part of implementation detail.
 */
final class JsonInternal {
	@Nullable
	static Object readTree(JsonStreamReader reader) throws IOException, MalformedSyntaxException {
		switch (reader.peek()) {
		case BEGIN_ARRAY:
			reader.beginArray();

			List<Object> list = new ArrayList<>();

			while (reader.hasNext()) {
				list.add(readTree(reader));
			}

			reader.endArray();

			return list;
		case BEGIN_OBJECT:
			reader.beginObject();

			Map<String, Object> object = new LinkedHashMap<>();

			while (reader.hasNext()) {
				String key = reader.nextName();
				object.put(key, readTree(reader));
			}

			reader.endObject();

			return object;
		case STRING:
			return reader.nextString();
		case NUMBER:
			return reader.nextNumber();
		case BOOLEAN:
			return reader.nextBoolean();
		// Invalid elements to read?
		case NAME:
		case END_ARRAY:
		case END_OBJECT:
		case NULL:
		case END_DOCUMENT:
		default:
			throw new MalformedSyntaxException(reader, "Invalid element encountered");
		}
	}

	private JsonInternal() {
	}
}
