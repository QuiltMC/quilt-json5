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
import org.junit.jupiter.api.TestFactory;
import org.quiltmc.json5.api.JsonApi;
import org.quiltmc.json5.api.JsonArrayVisitor;
import org.quiltmc.json5.api.JsonObjectVisitor;
import org.quiltmc.json5.api.JsonVisitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

class Test {
	@TestFactory
	Stream<DynamicTest> testOfficialSuite() throws IOException {
		Path tests = Paths.get("json5-tests");
		return Files.walk(tests).filter(path -> {
			String str = path.toString();
			return !str.startsWith("json5-tests/.") && (str.endsWith(".json") || str.endsWith(".json5") || str.endsWith(".js") || str.endsWith(".txt"));
		}).map(path ->
			DynamicTest.dynamicTest(path.toString(), () -> {
				if (Files.isDirectory(path)) {
					System.out.print(path);
				}
				try {
					if (path.toString().endsWith(".json") || path.toString().endsWith(".json5")) {
						System.out.println(path.toString());
						JsonApi.visit(new String(Files.readAllBytes(path)), new BasicVisitor());
						System.out.println("PASSED as expected");
						return;
					}
				} catch (Exception ex) {
					throw new RuntimeException("org.quiltmc.json5.test.Test " + path.getFileName() + " failed!", ex);
				}

				try {
					if (path.toString().endsWith(".js") || path.toString().endsWith(".txt")) {
						System.out.println(path.toString());
						JsonApi.visit(new String(Files.readAllBytes(path)), new BasicVisitor());
						throw new RuntimeException("org.quiltmc.json5.test.Test " + path.getFileName() + " failed! It should have not parsed correctly, but it did!");
					}


				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} catch (Exception ex) {
					System.out.println("FAILED as expected");
				}
			})
		);
	}
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
