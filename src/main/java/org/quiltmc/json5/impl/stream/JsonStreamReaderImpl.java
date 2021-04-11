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

package org.quiltmc.json5.impl.stream;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.json5.api.JsonNull;
import org.quiltmc.json5.api.JsonToken;
import org.quiltmc.json5.api.exception.FormatViolationException;
import org.quiltmc.json5.api.stream.JsonStreamReader;

import java.util.*;

@ApiStatus.Internal
@SuppressWarnings({"unchecked", "rawtypes"})
public final class JsonStreamReaderImpl implements JsonStreamReader {
	private final Queue<Value> values = new LinkedList<>();
	public JsonStreamReaderImpl(Object object) {
		createValue(object);
	}

	private void createValue(Object object) {
		if (object instanceof Map) {
			values.add(Value.BEGIN_OBJECT);
			((Map) object).forEach((k, v) -> {
				values.add(new Value(JsonToken.NAME, k));
				createValue(v);
			});
			values.add(Value.END_OBJECT);
		} else if (object instanceof Collection) {
			values.add(Value.BEGIN_ARRAY);
			((Collection) object).forEach(this::createValue);
			values.add(Value.END_ARRAY);
		} else {
			if (object instanceof String) {
				values.add(new Value(JsonToken.STRING, object));
			} else if (object instanceof Number) {
				values.add(new Value(JsonToken.NUMBER, object));
			} else if (object instanceof Boolean) {
				values.add(new Value(JsonToken.BOOLEAN, object));
			} else if (object instanceof JsonNull) {
				values.add(Value.NULL);
			} else {
				throw new IllegalStateException();
			}
		}
	}

	@Override
	public void beginArray() {
		Value value = values.poll();
		if (value.type != JsonToken.BEGIN_ARRAY) {
			throw new FormatViolationException("Expected a BEGIN_ARRAY but found " + value);
		}
	}

	@Override
	public void endArray() {
		Value value = values.poll();
		if (value.type != JsonToken.END_ARRAY) {
			throw new FormatViolationException("Expected a END_ARRAY but found " + value);
		}
	}

	@Override
	public void beginObject() {
		Value value = values.poll();
		if (value.type != JsonToken.BEGIN_OBJECT) {
			throw new FormatViolationException("Expected a BEGIN_OBJECT but found " + value);
		}
	}

	@Override
	public void endObject() {
		Value value = values.poll();
		if (value.type != JsonToken.END_OBJECT) {
			throw new FormatViolationException("Expected a END_OBJECT but found " + value);
		}
	}

	@Override
	public boolean hasNext() {
		return values.isEmpty();
	}

	@Override
	public JsonToken peek() {
		return values.peek().type;
	}

	@Override
	public String nextName() {
		Value value = values.poll();

		if (value.type != JsonToken.NAME) {
			throw new FormatViolationException("Expected a NAME but found " + value);
		}

		return (String) value.value;
	}

	@Override
	public String nextString() {
		Value value = values.poll();

		if (value.type != JsonToken.STRING) {
			throw new FormatViolationException("Expected a STRING but found " + value);
		}

		return (String) value.value;
	}

	@Override
	public boolean nextBoolean() {
		Value value = values.poll();

		if (value.type != JsonToken.BOOLEAN) {
			throw new FormatViolationException("Expected a BOOLEAN but found " + value);
		}

		return (Boolean) value.value;
	}

	@Override
	public void nextNull() {
		Value value = values.poll();

		if (value.type != JsonToken.NULL) {
			throw new FormatViolationException("Expected a NULL but found " + value);
		}
	}

	@Override
	public double nextDouble() {
		Value value = values.poll();

		if (value.type != JsonToken.NUMBER) {
			throw new FormatViolationException("Expected a NUMBER but found " + value);
		}

		return ((Number) value.value).doubleValue();
	}

	@Override
	public long nextLong() {
		Value value = values.poll();

		if (value.type != JsonToken.NUMBER) {
			throw new FormatViolationException("Expected a NUMBER but found " + value);
		}

		return ((Number) value.value).longValue();
	}

	@Override
	public int nextInt() {
		Value value = values.poll();

		if (value.type != JsonToken.NUMBER) {
			throw new FormatViolationException("Expected a NUMBER but found " + value);
		}

		return ((Number) value.value).intValue();
	}

	@Override
	public void close() {

	}

	@Override
	public void skipValue() {
		JsonToken type = values.poll().type;
		int stack = 1;
		// NOTE: in an actual parser we would need to be sure the objects + arrays beginnings and endings agree,
		// but since we manage the queue we know this is sound
		if (type == JsonToken.BEGIN_ARRAY || type == JsonToken.BEGIN_OBJECT) {
			while (stack != 0) {
				JsonToken next = values.poll().type;
				if (next == JsonToken.BEGIN_ARRAY || next == JsonToken.BEGIN_OBJECT) {
					stack++;
				} else if (next == JsonToken.END_ARRAY || next == JsonToken.END_OBJECT) {
					stack--;
				}
			}
		}
		if (type == JsonToken.NAME) {
			skipValue();
		}
	}


	private static class Value {
		final JsonToken type;
		final Object value;
		static final Value BEGIN_ARRAY = new Value(JsonToken.BEGIN_ARRAY, null);
		static final Value BEGIN_OBJECT = new Value(JsonToken.BEGIN_OBJECT, null);
		static final Value END_ARRAY = new Value(JsonToken.END_ARRAY, null);
		static final Value END_OBJECT = new Value(JsonToken.END_OBJECT, null);
		static final Value NULL = new Value(JsonToken.NULL, null);
		public Value(JsonToken type, Object value) {
			this.type = type;
			this.value = value;
		}
	}
}
