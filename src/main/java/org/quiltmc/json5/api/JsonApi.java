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
import org.quiltmc.json5.impl.TreeVisitor;
import org.quiltmc.json5.impl.parser.json5.Json5Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonApi {
	/**
	 * Parses a JSON5 file into a tree of its values.
	 * @return A {@link String}, {@link Boolean}, {@link Number}, {@link JsonNull},
	 * or a {@link java.util.LinkedHashMap}&lt;String, Object&gt; or {@link java.util.List}&lt;Object&gt; containing any of these types.
	 */
	public static Object parseToTree(Path path) {
		try {
			return parseToTree(new String(Files.readAllBytes(path)));
		} catch (IOException ex) {
			throw new ParseException("Unable to read file: ", ex);
		}
	}

	/**
	 * Parses JSON5 text into a tree of its values.
	 * @return A {@link String}, {@link Boolean}, {@link Number}, {@link JsonNull},
	 * or a {@link java.util.LinkedHashMap}&lt;String, Object&gt; or {@link java.util.List}&lt;Object&gt; containing any of these types.
	 */
	public static Object parseToTree(String text) {
		TreeVisitor visitor = new TreeVisitor();
		visit(text, visitor);
		return visitor.root;
	}

	/**
	 * @throws ParseException if the path could not be read or parsed
	 * @throws org.quiltmc.json5.api.exception.FormatViolationException if the text does not follow the format expected by the visitor.
	 */
	public static void visit(Path path, JsonVisitor visitor) {
		try {
			visit(new String(Files.readAllBytes(path)), visitor);
		} catch (IOException ex) {
			throw new ParseException("Unable to read file: ", ex);
		}
	}

	/**
	 * @throws ParseException if the text could not be parsed
	 * @throws org.quiltmc.json5.api.exception.FormatViolationException if the text does not follow the format expected by the visitor.
	 */
	public static void visit(String text, JsonVisitor visitor) {
		new Json5Parser(text, visitor).parse();
	}
}
