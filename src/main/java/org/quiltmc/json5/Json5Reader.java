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

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.json5.api.JsonReader;

import java.util.ArrayDeque;
import java.util.Deque;

@ApiStatus.Internal
public class Json5Reader implements JsonReader {
	private Deque<State> stack = new ArrayDeque<>();
	private Json5Parser parser = null;

	@Override
	public String peekKey() {
		return null;
	}

	@Override
	public ElementType peek() {
		return null;
	}

	@Override
	public boolean next() {
		return false;
	}

	@Override
	public void pushObject() {
		this.stack.push(State.OBJECT);
	}

	@Override
	public void pushArray() {
		this.stack.push(State.ARRAY);
	}

	@Override
	public void pop() {
		this.stack.pop();
	}


	@Override
	public String readString() {
		return null;
	}

	@Override
	public boolean readBoolean() {
		return false;
	}

	@Override
	public Number readNumber() {
		return null;
	}

	@Override
	public int readInt() {
		return 0;
	}

	@Override
	public double readDouble() {
		return 0;
	}

	@Override
	public void readNull() {

	}

	@Override
	public void close() throws Exception {

	}

	private enum State {
		OBJECT,
		ARRAY
	}
}
