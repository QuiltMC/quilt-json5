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

package org.quiltmc.json5.impl.wrapper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.visitor.JsonArrayVisitor;

@ApiStatus.Internal
public final class ArrayWrapper implements VisitorWrapper {
	private final JsonArrayVisitor visitor;
	ArrayWrapper(JsonArrayVisitor visitor) {
		this.visitor = visitor;
	}
	@Override
	public @NotNull VisitorWrapper visitObject(@Nullable String name) {
		checkKey(name);
		return new ObjectWrapper(this.visitor.visitObject());
	}

	@Override
	public @NotNull VisitorWrapper visitArray(@Nullable String name) {
		checkKey(name);
		return new ArrayWrapper(this.visitor.visitArray());
	}

	@Override
	public void visitString(@Nullable String name, String value) {
		checkKey(name);
		this.visitor.visitString(value);
	}

	@Override
	public void visitBoolean(@Nullable String name, boolean value) {
		checkKey(name);
		this.visitor.visitBoolean(value);
	}

	@Override
	public void visitNumber(@Nullable String name, Number value) {
		checkKey(name);
		this.visitor.visitNumber(value);
	}

	@Override
	public void visitNull(@Nullable String name) {
		checkKey(name);
		this.visitor.visitNull();
	}


	private static <T> void checkKey(T in) {
		if (in != null) {
			throw new IllegalStateException("Parser tried to pass an object value to an array? Something is very wrong!");
		}
	}
}
