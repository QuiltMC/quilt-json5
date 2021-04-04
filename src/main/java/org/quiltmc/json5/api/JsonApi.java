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
