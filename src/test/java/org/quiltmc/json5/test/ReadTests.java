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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.quiltmc.json5.api.JsonApi;
import org.quiltmc.json5.api.stream.JsonStreamReader;
import org.quiltmc.json5.api.visitor.JsonArrayVisitor;
import org.quiltmc.json5.api.visitor.JsonObjectVisitor;
import org.quiltmc.json5.api.visitor.JsonVisitor;
import org.quiltmc.json5.api.visitor.writer.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

class ReadTests {
	@TestFactory
	Stream<DynamicTest> validJson5() throws IOException {
		return Files.walk(Paths.get("tests")).filter(path -> {
			String str = path.toString();
			return !Files.isDirectory(path) && !str.startsWith("json5-tests/.") && (str.endsWith(".json") || str.endsWith(".json5"));
		}).map(path -> DynamicTest.dynamicTest("Valid JSON5: " + path, () -> JsonApi.visit(path, new BasicVisitor())));
	}

	@TestFactory
	Stream<DynamicTest> json5IsInvalidJson() throws IOException {
		return Files.walk(Paths.get("tests")).filter(path -> {
			String str = path.toString();
			return !Files.isDirectory(path) && !str.startsWith("json5-tests/.") && str.endsWith(".json5");
		}).map(path ->
			DynamicTest.dynamicTest("Strict: " + path, () -> {
				JsonStreamReader reader = JsonApi.streamReader(path);
				reader.setStrictJson();
				try {
					JsonApi.visit(reader, new BasicVisitor());
				} catch (Throwable t) {
					//TODO: make sure it's not a reading the file wrong error?
					return;
				}
				throw new RuntimeException("Invalid JSON successfully parsed as strict json?");
			})
		);
	}

	@TestFactory
	Stream<DynamicTest> invalidJson5() throws IOException {
		return Files.walk(Paths.get("tests")).filter(path -> {
			String str = path.toString();
			return !Files.isDirectory(path) && !str.startsWith("json5-tests/.") && (str.endsWith(".js") || str.endsWith(".txt"));
		}).map(path ->
				DynamicTest.dynamicTest("Strict: " + path, () -> {
					try {
						JsonApi.visit(path, new BasicVisitor());
					} catch (Throwable t) {
						//TODO: make sure it's not a reading the file wrong error?
					}
				})
		);
	}

	class BasicVisitor implements JsonVisitor {
		@Override
		public JsonObjectVisitor rootObject() {
			System.out.println("\tobject");
			return new BasicObjectVisitor(2);
		}

		@Override
		public JsonArrayVisitor rootArray() {
			System.out.println("\tarray");
			return new BasicArrayVisitor(2);
		}

		@Override
		public void rootString(String string) {
			System.out.println("\tstring: " + string);
		}

		@Override
		public void rootBoolean(boolean bool) {
			System.out.println("\tboolean: " + bool);
		}

		@Override
		public void rootNumber(Number number) {
			System.out.println("\tnumber: " + number);
		}

		@Override
		public void rootNull() {
			System.out.println("\tnull");
		}
	}

	class BasicObjectVisitor implements JsonObjectVisitor {
		private final String tabs;
		private final int indentation;
		public BasicObjectVisitor(int indentation) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indentation; i++) {
				sb.append("\t");
			}
			tabs = sb.toString();
			this.indentation = indentation;
		}

		@Override
		public @NotNull JsonObjectVisitor visitObject(String name) {
			System.out.println(tabs + "object: " + name);
			return new BasicObjectVisitor(indentation + 1);
		}

		@Override
		public @NotNull JsonArrayVisitor visitArray(String name) {
			System.out.println(tabs + "array: " + name);
			return new BasicArrayVisitor(indentation + 1);
		}

		@Override
		public void visitString(String name, String value) {
			System.out.println(tabs + "string: " + name + " " + value);
		}

		@Override
		public void visitBoolean(String name, boolean value) {
			System.out.println(tabs + "boolean: " + name + " " + value);
		}

		@Override
		public void visitNumber(String name, Number value) {
			System.out.println(tabs + "number: " + name + " " + value);
		}

		@Override
		public void visitNull(String name) {
			System.out.println(tabs + "null: " + name);
		}
	}

	class BasicArrayVisitor implements JsonArrayVisitor {
		private final String tabs;
		private final int indentation;
		public BasicArrayVisitor(int indentation) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indentation; i++) {
				sb.append("\t");
			}
			tabs = sb.toString();
			this.indentation = indentation;
		}
		@Override
		public @NotNull JsonObjectVisitor visitObject() {
			System.out.println(tabs + "object");
			return new BasicObjectVisitor(indentation + 1);
		}

		@Override
		public @NotNull JsonArrayVisitor visitArray() {
			System.out.println(tabs + "array");
			return new BasicArrayVisitor(indentation + 1);
		}

		@Override
		public void visitString(String string) {
			System.out.println(tabs + "string: " + string);
		}

		@Override
		public void visitBoolean(boolean bool) {
			System.out.println(tabs + "boolean: " + bool);
		}

		@Override
		public void visitNumber(Number number) {
			System.out.println(tabs + "number: " + number);
		}

		@Override
		public void visitNull() {
			System.out.println(tabs + "null");
		}
	}
}