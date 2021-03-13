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

import java.nio.file.Path;

public interface JsonReader extends AutoCloseable {
	/**
	 * Peeks forward in the input, reading the next key of an object while leaving the cursor in place.
	 * @return the key
	 * @throws IllegalStateException if the object being read is not an object
	 */
	String peekKey();

	/**
	 * Peeks forward in the input, checking the type of the next element.
	 *
	 * @return the type of element being read
	 */
	ElementType peek();

	/**
	 * Checks if there is another element present within the current object or array being read.
	 * This will skip the previous value if there was no interest in the previous value.
	 *
	 * @return
	 */
	boolean next();

	/**
	 * Begins reading a json object. The first object is implicit.
	 */
	void pushObject();

	/**
	 * Begins reading a json array.
	 */
	void pushArray();

	/**
	 * Ends reading a json object or array.
	 */
	void pop();

	/**
	 * Reads the next json string in the input, moving the cursor to the next element.
	 *
	 * @return the string value
	 */
	String readString();

	/**
	 * Reads the next json boolean in the input, moving the cursor to the next element.
	 *
	 * @return the boolean value
	 */
	boolean readBoolean();

	/**
	 * Reads the next json number in the input, moving the cursor to the next element.
	 *
	 * @return the number value
	 */
	Number readNumber();

	/**
	 * Reads the next json number in the input coercing the value to an integer, moving the cursor to the next element.
	 *
	 * @return the integer value
	 */
	int readInt();

	/**
	 * Reads the next json number in the input coercing the value to a double, moving the cursor to the next element.
	 *
	 * @return the integer value
	 */
	double readDouble();

	/**
	 * Reads the next json null in the input, moving the cursor to the next element.
	 */
	void readNull();

	static JsonReader json5(Path path) {
		// return new Json5Parser(path)
		throw new AssertionError("Not implemented!");
	}
	enum ElementType {
		OBJECT,
		ARRAY,
		STRING,
		NUMBER,
		BOOLEAN,
		NULL
	}
}
