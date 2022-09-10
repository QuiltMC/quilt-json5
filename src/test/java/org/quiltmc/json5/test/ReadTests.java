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

package org.quiltmc.json5.test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import org.quiltmc.json5.exception.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadTests {
	@TestFactory
	Stream<DynamicTest> validJson5() throws IOException {
		return Files.walk(Paths.get("tests").resolve("json5-tests")).filter(path -> {
			String str = path.toString();
			return !Files.isDirectory(path) && !str.startsWith("json5-tests/.") && (str.endsWith(".json") || str.endsWith(".json5"));
		}).map(path -> DynamicTest.dynamicTest("Valid JSON5: " + path, () -> {
			System.out.println();
			System.out.println(path);
			System.out.println();

			try (JsonReader reader = JsonReader.json5(path)) {
				read(reader);
			}
			// JsonApi.visit(path, new BasicVisitor());
		}));
	}

	@TestFactory
	Stream<DynamicTest> validStrictJson() throws IOException {
		return Files.walk(Paths.get("tests").resolve("json5-tests")).filter(path -> {
			String str = path.toString();
			return !Files.isDirectory(path) && !str.startsWith("json5-tests/.") && (str.endsWith(".json"));
		}).map(path -> DynamicTest.dynamicTest("Valid JSON: " + path, () -> {
			System.out.println();
			System.out.println(path);
			System.out.println();

			try (JsonReader reader = JsonReader.json(path)) {
				read(reader);
			}
			// JsonApi.visit(path, new BasicVisitor());
		}));
	}

	@TestFactory
	Stream<DynamicTest> json5IsInvalidJson() throws IOException {
		return Files.walk(Paths.get("tests").resolve("json5-tests")).filter(path -> {
			String str = path.toString();
			return !Files.isDirectory(path) && !str.startsWith("json5-tests/.") && str.endsWith(".json5");
		}).map(path ->
			DynamicTest.dynamicTest("Strict: " + path, () -> {
				System.out.println();
				System.out.println(path);
				System.out.println();

				try (JsonReader reader = JsonReader.json(path)) {
					read(reader);
				} catch (Throwable e) {
					// TODO: make sure it's not a reading the file wrong error?
					return;
				}

				throw new RuntimeException("Invalid JSON successfully parsed as strict json?");
			})
		);
	}

	@TestFactory
	Stream<DynamicTest> invalidJson5() throws IOException {
		return Files.walk(Paths.get("tests").resolve("json5-tests")).filter(path -> {
			String str = path.toString();
			return !Files.isDirectory(path) && !str.startsWith("json5-tests/.") && (str.endsWith(".js") || str.endsWith(".txt"));
		}).map(path ->
				DynamicTest.dynamicTest("Strict: " + path, () -> {
					System.out.println();
					System.out.println(path);
					System.out.println();

					try (JsonReader reader = JsonReader.json5(path)) {
						read(reader);
					} catch (Throwable t) {
						//TODO: make sure it's not a reading the file wrong error?
					}
				})
		);
	}

	static void read(JsonReader reader) throws IOException, ParseException {
		innerRead(reader, 0);

		// Make sure we hit end of document
		assertEquals(reader.peek(), JsonToken.END_DOCUMENT);
	}

	private static void innerRead(JsonReader reader, int depth) throws IOException {
		// TODO JDK Update: We likely will not be using Java 11 for a long time in this library unlike the toolchain.
		//  When we do move to something above 11 or above replace the stuff below with `String.repeat()`.

		switch (reader.peek()) {
		case BEGIN_ARRAY:
			System.out.printf("%sarray%n", new String(new char[depth]).replace("\0", "\t"));

			reader.beginArray();

			while (reader.hasNext()) {
				innerRead(reader, depth + 1);
			}

			reader.endArray();

			System.out.printf("%sendarray%n", new String(new char[depth]).replace("\0", "\t"));
			break;
		case BEGIN_OBJECT:
			System.out.printf("%sobject%n", new String(new char[depth]).replace("\0", "\t"));

			reader.beginObject();

			while (reader.hasNext()) {
				// Consume the key and print out the name of the key
				String key = reader.nextName();

				System.out.printf("%s\"%s\":", new String(new char[depth]).replace("\0", "\t"), key);
				innerRead(reader, depth + 1);
			}

			reader.endObject();

			System.out.printf("%sendobject%n", new String(new char[depth]).replace("\0", "\t"));
			break;
		case STRING:
			System.out.printf("%sstring: %s%n", new String(new char[depth]).replace("\0", "\t"), reader.nextString());

			break;
		case NUMBER:
			System.out.printf("%snumber: %s%n", new String(new char[depth]).replace("\0", "\t"), reader.nextNumber());

			break;
		case BOOLEAN:
			System.out.printf("%sboolean: %s%n", new String(new char[depth]).replace("\0", "\t"), reader.nextBoolean());

			break;
		case NULL:
			// Consume the null
			reader.nextNull();
			System.out.printf("%snull%n", new String(new char[depth]).replace("\0", "\t"));

			break;
		// End of elements, we will not reach these ever.
		case END_ARRAY:
		case END_OBJECT:
			throw new AssertionError("Reader reached invalid state?");
		case END_DOCUMENT:
			if (depth != 0) {
				throw new ParseException("Hit invalid end of document.");
			}

			break;
		case NAME:
			// Invalid
			throw new ParseException("Invalid name entry");
		}
	}
}
