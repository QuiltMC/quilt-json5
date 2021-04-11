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

import org.quiltmc.json5.api.JsonToken;
import org.quiltmc.json5.api.exception.MalformedSyntaxException;
import org.quiltmc.json5.api.stream.JsonStreamReader;
import org.quiltmc.json5.api.visitor.JsonVisitor;
import org.quiltmc.json5.impl.wrapper.ArrayWrapper;
import org.quiltmc.json5.impl.wrapper.ObjectWrapper;
import org.quiltmc.json5.impl.wrapper.VisitorWrapper;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

public final class ReaderToVisitorAdapter {
	public static void visit(JsonStreamReader reader, JsonVisitor root) throws IOException {
		Deque<VisitorWrapper> visitors = new LinkedList<>();
		switch (reader.peek()) {
			case BEGIN_ARRAY:
				reader.beginArray();
				visitors.push(VisitorWrapper.create(root.rootArray()));
				break;
			case BEGIN_OBJECT:
				reader.beginObject();
				visitors.push(VisitorWrapper.create(root.rootObject()));
				break;
			case STRING:
				root.rootString(reader.nextString());
				clearAndClose(reader);
				return;
			case NUMBER:
				root.rootNumber(reader.nextNumber());
				clearAndClose(reader);
				return;
			case BOOLEAN:
				root.rootBoolean(reader.nextBoolean());
				clearAndClose(reader);
				return;
			case NULL:
				reader.nextNull();
				root.rootNull();
				clearAndClose(reader);
				return;
			case END_DOCUMENT:
				clearAndClose(reader);
				return;
		}

		String name = null;
		loop: while (true) {
			VisitorWrapper visitor = visitors.peekLast();
			switch (reader.peek()) {
				case NAME:
					name = reader.nextName();
					continue; // Avoid setting name to null
				case BEGIN_ARRAY:
					reader.beginArray();
					visitors.addLast(visitor.visitArray(name));
					break;
				case END_ARRAY:
					reader.endArray();
					if (!(visitors.removeLast() instanceof ArrayWrapper)) {
						throw new IllegalStateException("Illegal end of array");
					}
					break;
				case BEGIN_OBJECT:
					reader.beginObject();
					visitors.addLast(visitor.visitObject(name));
					break;
				case END_OBJECT:
					reader.endObject();
					if (!(visitors.removeLast() instanceof ObjectWrapper)) {
						throw new IllegalStateException("Illegal end of object");
					}
					break;
				case STRING:
					visitor.visitString(name, reader.nextString());
					break;
				case NUMBER:
					visitor.visitNumber(name, reader.nextNumber());
					break;
				case BOOLEAN:
					visitor.visitBoolean(name, reader.nextBoolean());
					break;
				case NULL:
					reader.nextNull();
					visitor.visitNull(name);
					break;
				case END_DOCUMENT:
					if (visitors.size() > 0) {
						throw new IllegalStateException("Illegal end of document");
					}
					break loop;
				default:
					throw new AssertionError("lol");
			}

			name = null;
		}
	}


	private static void clearAndClose(JsonStreamReader reader) throws IOException {
		while (reader.hasNext()) {
			JsonToken peeked = reader.peek();
			switch (peeked) {
				case BEGIN_ARRAY:
				case BEGIN_OBJECT:
				case NAME:
				case STRING:
				case NUMBER:
				case BOOLEAN:
				case NULL:
					reader.close();
					throw new MalformedSyntaxException("Expected nothing left but the reader isn't empty");
				case END_DOCUMENT:
					reader.close();
					return;
			}
			reader.skipValue();
		}
	}
}
