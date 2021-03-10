package org.quiltmc.json5;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A reader which parsers a json5 file.
 */
public final class JsonReader implements AutoCloseable {
	public static JsonReader create(Reader reader) {
		Objects.requireNonNull(reader, "Reader cannot be null");

		return new JsonReader(reader);
	}

	public static JsonReader create(Path path) throws IOException, IllegalArgumentException {
		Objects.requireNonNull(path, "Path cannot be null");

		if (Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must refer to a file");
		}

		return new JsonReader(Files.newBufferedReader(path));
	}

	public static JsonReader create(InputStream stream) {
		Objects.requireNonNull(stream, "Input stream cannot be null");

		return new JsonReader(new InputStreamReader(stream));
	}

	public static JsonReader create(String input) {
		Objects.requireNonNull(input, "Input string cannot be null");

		return new JsonReader(new StringReader(input));
	}

	private final Reader reader;

	private JsonReader(Reader reader) {
		this.reader = reader;
	}

	/**
	 * Peeks forward in the input, reading the next key of an object while leaving the cursor in place.
	 * @return the key
	 * @throws IllegalStateException if the object being read is not an object
	 */
	public String peekKey() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Peeks forward in the input, checking the type of the next element.
	 *
	 * @return the type of element being read
	 */
	public ElementType peek() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Checks if there is another element present within the current object or array being read.
	 * This will skip the previous value if there was no interest in the previous value.
	 *
	 * @return
	 */
	public boolean next() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Begins reading a json object.
	 */
	public void enterObject() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Begins reading a json array.
	 */
	public void enterArray() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Reads the next json string in the input, moving the cursor to the next element.
	 *
	 * @return the string value
	 */
	public String readString() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Reads the next json boolean in the input, moving the cursor to the next element.
	 *
	 * @return the boolean value
	 */
	public boolean readBoolean() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Reads the next json number in the input, moving the cursor to the next element.
	 *
	 * @return the number value
	 */
	public Number readNumber() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Reads the next json number in the input coercing the value to an integer, moving the cursor to the next element.
	 *
	 * @return the integer value
	 */
	public int readInt() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Reads the next json number in the input coercing the value to a double, moving the cursor to the next element.
	 *
	 * @return the integer value
	 */
	public double readDouble() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * Reads the next json null in the input, moving the cursor to the next element.
	 */
	public void readNull() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * @return the row that the json reader's cursor is currently at.
	 */
	public int row() {
		throw new UnsupportedOperationException("Implement me");
	}

	/**
	 * @return the column that the json reader's cursor is currently at.
	 */
	public int column() {
		throw new UnsupportedOperationException("Implement me");
	}

	@Override
	public void close() throws Exception {
		this.reader.close();
	}

	public enum ElementType {
		OBJECT,
		ARRAY,
		STRING,
		NUMBER,
		BOOLEAN,
		NULL
	}
}
