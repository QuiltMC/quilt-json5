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

package org.quiltmc.json5;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static org.quiltmc.json5.JsonScope.DANGLING_NAME;
import static org.quiltmc.json5.JsonScope.EMPTY_ARRAY;
import static org.quiltmc.json5.JsonScope.EMPTY_DOCUMENT;
import static org.quiltmc.json5.JsonScope.EMPTY_OBJECT;
import static org.quiltmc.json5.JsonScope.NONEMPTY_ARRAY;
import static org.quiltmc.json5.JsonScope.NONEMPTY_DOCUMENT;
import static org.quiltmc.json5.JsonScope.NONEMPTY_OBJECT;

/*
 * Gson is Copyright (C) 2010 Google Inc, under the Apache License Version 2.0 (the same as in the header above).
 *
 * The following changes have bee applied:
 * - The lenient mode has been replaced with a json5 mode, which only writes strict json5.
 * - A strict JSON only mode has been added which will only write valid JSON.
 * - Writer changes to handle JSON5 syntax extensions.
 * - Add ability to write comments into the writer.
 * - Repackaged.
 * - Other minor code style changes.
 *
 * You may view the original, including its license header, here:
 * https://github.com/google/gson/blob/530cb7447089ccc12dc2009c17f468ddf2cd61ca/gson/src/main/java/com/google/gson/stream/JsonReader.java
 */

/**
 * @deprecated Please migrate to <a href="https://github.com/QuiltMC/qup">QUP JSON</a>. Note that QUP is not exposed
 * by Quilt Loader; consider shading it!
 */
@Deprecated
public final class JsonWriter implements Closeable, Flushable {
	/*
	 * From RFC 7159, "All Unicode characters may be placed within the
	 * quotation marks except for the characters that must be escaped:
	 * quotation mark, reverse solidus, and the control characters
	 * (U+0000 through U+001F)."
	 *
	 * We also escape '\u2028' and '\u2029', which JavaScript interprets as
	 * newline characters. This prevents eval() from failing with a syntax
	 * error. http://code.google.com/p/google-gson/issues/detail?id=341
	 */
	private static final String[] REPLACEMENT_CHARS;
	private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
	static {
		REPLACEMENT_CHARS = new String[128];
		for (int i = 0; i <= 0x1f; i++) {
			REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
		}
		REPLACEMENT_CHARS['"'] = "\\\"";
		REPLACEMENT_CHARS['\\'] = "\\\\";
		REPLACEMENT_CHARS['\t'] = "\\t";
		REPLACEMENT_CHARS['\b'] = "\\b";
		REPLACEMENT_CHARS['\n'] = "\\n";
		REPLACEMENT_CHARS['\r'] = "\\r";
		REPLACEMENT_CHARS['\f'] = "\\f";
		HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
		HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
		HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
		HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
		HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
	}

	/** The output data, containing at most one top-level array or object. */
	private final Writer out;

	private int[] stack = new int[32];
	private int stackSize = 0;
	{
		push(EMPTY_DOCUMENT);
	}

	/**
	 * A string containing a full set of spaces for a single level of
	 * indentation, or null for no pretty printing.
	 */
	private String indent = "\t";

	/**
	 * The name/value separator; either ":" or ": ".
	 */
	private String separator = ": ";

	private boolean htmlSafe;

	private String deferredName;

	private String deferredComment;
	boolean inlineWaited = false;
	private boolean strict = false;

	private boolean compact = false;

	private boolean serializeNulls = true;

	// API methods

	/**
	 * Creates a new instance that writes a JSON5-encoded stream.
	 */
	public static JsonWriter json5(Path out) throws IOException {
		return json5(Files.newBufferedWriter(Objects.requireNonNull(out, "Path cannot be null")));
	}

	/**
	 * Creates a new instance that writes a JSON5-encoded stream to {@code out}.
	 * For best performance, ensure {@link Writer} is buffered; wrapping in
	 * {@link java.io.BufferedWriter BufferedWriter} if necessary.
	 */
	public static JsonWriter json5(Writer out) {
		return new JsonWriter(out);
	}

	/**
	 * Creates a new instance that writes a strictly JSON-encoded stream.
	 * This disables NaN, (+/-)Infinity, and comments, and enables quotes around keys.
	 */
	public static JsonWriter json(Path out) throws IOException {
		return json5(out).setStrictJson();
	}

	/**
	 * Creates a new instance that writes a strictly JSON-encoded stream to {@code out}.
	 * This disables NaN, (+/-)Infinity, and comments, and enables quotes around keys.
	 * For best performance, ensure {@link Writer} is buffered; wrapping in
	 * {@link java.io.BufferedWriter BufferedWriter} if necessary.
	 */
	public static JsonWriter json(Writer out) {
		return json5(out).setStrictJson();
	}

	private JsonWriter(Writer out) {
		if (out == null) {
			throw new NullPointerException("out == null");
		}
		this.out = out;
	}

	/**
	 * Sets the indentation string to be repeated for each level of indentation
	 * in the encoded document. If {@code indent.isEmpty()} the encoded document
	 * will be compact. Otherwise the encoded document will be more
	 * human-readable.
	 *
	 * @param indent a string containing only whitespace.
	 */
	public void setIndent(String indent) {
		if (indent.length() == 0) {
			this.compact = true;
			this.indent = null;
			this.separator = ":";
		} else {
			this.compact = false;
			this.indent = indent;
			this.separator = ": ";
		}
	}

	/**
	 * Configure if the output must be strict JSON, instead of strict JSON5. This flag disables NaN, (+/-)Infinity, comments, and enables quotes around keys.
	 */
	public JsonWriter setStrictJson() {
		this.strict = true;
		return this;
	}

	/**
	 * Returns true if the output must be strict JSON, instead of strict JSON5. The default is false.
	 */
	public boolean isStrictJson() {
		return strict;
	}

	/**
	 * Shortcut for {@code setIndent("")} that makes the encoded document significantly more compact.
	 */
	public void setCompact() {
		setIndent("");
	}

	/**
	 * Returns true if the output will be compact (entirely one line) and false if it will be human-readable with newlines and indentation.
	 * The default is false.
	 */
	public boolean isCompact() {
		return indent == null;
	}

	/**
	 * Configure this writer to emit JSON that's safe for direct inclusion in HTML
	 * and XML documents. This escapes the HTML characters {@code <}, {@code >},
	 * {@code &} and {@code =} before writing them to the stream. Without this
	 * setting, your XML/HTML encoder should replace these characters with the
	 * corresponding escape sequences.
	 */
	public final void setHtmlSafe(boolean htmlSafe) {
		this.htmlSafe = htmlSafe;
	}

	/**
	 * Returns true if this writer writes JSON that's safe for inclusion in HTML
	 * and XML documents.
	 */
	public final boolean isHtmlSafe() {
		return htmlSafe;
	}

	/**
	 * Sets whether object members are serialized when their value is null.
	 * This has no impact on array elements. The default is true.
	 */
	public void setSerializeNulls(boolean serializeNulls) {
		this.serializeNulls = serializeNulls;
	}

	/**
	 * Returns true if object members are serialized when their value is null.
	 * This has no impact on array elements. The default is true.
	 */
	public boolean getSerializeNulls() {
		return serializeNulls;
	}

	/**
	 * Encodes the property name.
	 *
	 * @param name the name of the forthcoming value. May not be null.
	 * @return this writer.
	 */
	public JsonWriter name(String name) throws IOException {
		if (name == null) {
			throw new NullPointerException("name == null");
		}
		if (deferredName != null) {
			throw new IllegalStateException();
		}
		if (stackSize == 0) {
			throw new IllegalStateException("JsonWriter is closed.");
		}
		deferredName = name;
		return this;
	}

	/**
	 * Begins encoding a new object. Each call to this method must be paired
	 * with a call to {@link #endObject}.
	 *
	 * @return this writer.
	 */
	public JsonWriter beginObject() throws IOException {
		writeDeferredName();
		return open(EMPTY_OBJECT, '{');
	}

	/**
	 * Ends encoding the current object.
	 *
	 * @return this writer.
	 */
	public JsonWriter endObject() throws IOException {
		return close(EMPTY_OBJECT, NONEMPTY_OBJECT, '}');
	}

	/**
	 * Begins encoding a new array. Each call to this method must be paired with
	 * a call to {@link #endArray}.
	 *
	 * @return this writer.
	 */
	public JsonWriter beginArray() throws IOException {
		writeDeferredName();
		return open(EMPTY_ARRAY, '[');
	}

	/**
	 * Ends encoding the current array.
	 *
	 * @return this writer.
	 */
	public JsonWriter endArray() throws IOException {
		return close(EMPTY_ARRAY, NONEMPTY_ARRAY, ']');
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public JsonWriter value(String value) throws IOException {
		if (value == null) {
			return nullValue();
		}
		writeDeferredName();
		beforeValue();
		string(value, true, true);
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonWriter value(boolean value) throws IOException {
		writeDeferredName();
		beforeValue();
		out.write(value ? "true" : "false");
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonWriter value(@Nullable Boolean value) throws IOException {
		if (value == null) {
			return nullValue();
		}
		writeDeferredName();
		beforeValue();
		out.write(value ? "true" : "false");
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities}.
	 * @return this writer.
	 */
	public JsonWriter value(Number value) throws IOException {
		if (value == null) {
			return nullValue();
		}

		writeDeferredName();
		String string = value.toString();
		if (strict && (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
			throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
		}
		beforeValue();
		out.append(string);
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities}.
	 * @return this writer.
	 */
	public JsonWriter value(double value) throws IOException {
		writeDeferredName();
		if (strict && (Double.isNaN(value) || Double.isInfinite(value))) {
			throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
		}
		beforeValue();
		out.append(Double.toString(value));
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public JsonWriter value(long value) throws IOException {
		writeDeferredName();
		beforeValue();
		out.write(Long.toString(value));
		return this;
	}

	/**
	 * Encodes {@code null}.
	 *
	 * @return this writer.
	 */
	public JsonWriter nullValue() throws IOException {
		if (deferredName != null) {
			if (serializeNulls) {
				writeDeferredName();
			} else {
				deferredName = null;
				return this; // skip the name and the value
			}
		}
		beforeValue();
		out.write("null");
		return this;
	}

	/**
	 * Writes {@code value} directly to the writer without quoting or
	 * escaping.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public JsonWriter jsonValue(String value) throws IOException {
		if (value == null) {
			return nullValue();
		}
		writeDeferredName();
		beforeValue();
		out.append(value);
		return this;
	}

	/**
	 * Encodes a comment, handling newlines and HTML safety gracefully.
	 * Silently does nothing when strict JSON mode is enabled.
	 * @param comment the comment to write, or null to encode nothing.
	 */
	public JsonWriter comment(String comment) throws IOException {
		if (compact || strict || comment == null) {
			return this;
		}

		if (deferredComment == null) {
			deferredComment = comment;
		} else {
			deferredComment += "\n" + comment;
		}

		// Be aggressive about writing comments if we are at the end of the document
		if (stackSize == 1 && peek() == NONEMPTY_DOCUMENT) {
			out.append('\n');
			writeDeferredComment();
		}

		return this;
	}

	/**
	 * This has not been implemented yet.
	 */
	public JsonWriter blockComment(@Nullable String comment) throws IOException {
		// TODO implement me!
		return comment(comment);
	}

	/**
	 * Ensures all buffered data is written to the underlying {@link Writer}
	 * and flushes that writer.
	 */
	public void flush() throws IOException {
		if (stackSize == 0) {
			throw new IllegalStateException("JsonWriter is closed.");
		}
		out.flush();
	}

	/**
	 * Flushes and closes this writer and the underlying {@link Writer}.
	 *
	 * @throws IOException if the JSON document is incomplete.
	 */
	public void close() throws IOException {
		out.close();

		int size = stackSize;
		if (size > 1 || size == 1 && stack[size - 1] != NONEMPTY_DOCUMENT) {
			throw new IOException("Incomplete document");
		}
		stackSize = 0;
	}

	// Implementation methods
	// Everything below here should be package-private or private

	/**
	 * Enters a new scope by appending any necessary whitespace and the given
	 * bracket.
	 */
	private JsonWriter open(int empty, char openBracket) throws IOException {
		beforeValue();
		push(empty);
		out.write(openBracket);
		return this;
	}

	/**
	 * Closes the current scope by appending any necessary whitespace and the
	 * given bracket.
	 */
	private JsonWriter close(int empty, int nonempty, char closeBracket)
			throws IOException {
		int context = peek();
		if (context != nonempty && context != empty) {
			throw new IllegalStateException("Nesting problem.");
		}
		if (deferredName != null) {
			throw new IllegalStateException("Dangling name: " + deferredName);
		}

		stackSize--;
		if (context == nonempty) {
			commentAndNewline();
		}
		out.write(closeBracket);
		return this;
	}

	private void push(int newTop) {
		if (stackSize == stack.length) {
			stack = Arrays.copyOf(stack, stackSize * 2);
		}
		stack[stackSize++] = newTop;
	}

	/**
	 * Returns the value on the top of the stack.
	 */
	private int peek() {
		if (stackSize == 0) {
			throw new IllegalStateException("JsonWriter is closed.");
		}
		return stack[stackSize - 1];
	}

	/**
	 * Replace the value on the top of the stack with the given value.
	 */
	private void replaceTop(int topOfStack) {
		stack[stackSize - 1] = topOfStack;
	}

	private void writeDeferredName() throws IOException {
		if (deferredName != null) {
			beforeName();
			boolean quotes = true;
			if (!strict) {
				// JSON5 allows bare names... only for keys that are valid EMCA5 identifiers
				// luckily, Java identifiers follow the same standard,
				//  so we can just use the built-in Character.isJavaIdentifierStart/Part methods
				if (deferredName.length() > 0) {
					if (Character.isJavaIdentifierStart(deferredName.charAt(0))) {
						quotes = false;
						for (int i = 1; i < deferredName.length(); i++) {
							if (!Character.isJavaIdentifierPart(deferredName.charAt(i))) {
								quotes = true;
								break;
							}
						}
					}
				}
			}

			string(deferredName, quotes, true);
			deferredName = null;
		}
	}

	private void writeDeferredComment() throws IOException {
		if (deferredComment == null) {
			return;
		}

		for (String s : deferredComment.split("\n")) {
			for (int i = 1, size = stackSize; i < size; i++) {
				out.write(indent);
			}
			out.write("// ");
			string(s, false, false);
			out.write("\n");
		}

		deferredComment = null;
	}

	private void string(String value, boolean quotes, boolean escapeQuotes) throws IOException {
		String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
		if (quotes) {
			out.write('\"');
		}

		if (!escapeQuotes) {
			replacements['\"'] = null;
		}

		int last = 0;
		int length = value.length();

		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			String replacement;
			if (c < 128) {
				replacement = replacements[c];
				if (replacement == null) {
					continue;
				}
			} else if (c == '\u2028') {
				replacement = "\\u2028";
			} else if (c == '\u2029') {
				replacement = "\\u2029";
			} else {
				continue;
			}
			if (last < i) {
				out.write(value, last, i - last);
			}
			out.write(replacement);
			last = i + 1;
		}
		if (last < length) {
			out.write(value, last, length - last);
		}

		if (quotes) {
			out.write('\"');
		}
	}

	private void commentAndNewline() throws IOException {
		if (indent == null) {
			return;
		}

		out.write('\n');
		writeDeferredComment();

		for (int i = 1, size = stackSize; i < size; i++) {
			out.write(indent);
		}
	}

	/**
	 * Inserts any necessary separators and whitespace before a name. Also
	 * adjusts the stack to expect the name's value.
	 */
	private void beforeName() throws IOException {
		int context = peek();
		if (context == NONEMPTY_OBJECT) { // first in object
			out.write(',');
		} else if (context != EMPTY_OBJECT) { // not in an object!
			throw new IllegalStateException("Nesting problem.");
		}
		commentAndNewline();
		replaceTop(DANGLING_NAME);
	}

	/**
	 * Inserts any necessary comments, separators, and whitespace before a literal value,
	 * inline array, or inline object. Also adjusts the stack to expect either a
	 * closing bracket or another element.
	 */
	@SuppressWarnings("fallthrough")
	private void beforeValue() throws IOException {
		switch (peek()) {
			case NONEMPTY_DOCUMENT:
				// TODO: This isn't a JSON5 feature, right?
				throw new IllegalStateException(
						"JSON must have only one top-level value.");
				// fall-through
			case EMPTY_DOCUMENT: // first in document
				writeDeferredComment();
				replaceTop(NONEMPTY_DOCUMENT);
				break;

			case EMPTY_ARRAY: // first in array
				replaceTop(NONEMPTY_ARRAY);
				commentAndNewline();
				break;

			case NONEMPTY_ARRAY: // another in array
				out.append(',');
				commentAndNewline();
				break;

			case DANGLING_NAME: // value for name
				out.append(separator);
				replaceTop(NONEMPTY_OBJECT);
				break;

			default:
				throw new IllegalStateException("Nesting problem.");
		}
	}
}