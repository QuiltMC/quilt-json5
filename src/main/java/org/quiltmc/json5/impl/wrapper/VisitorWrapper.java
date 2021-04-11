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
import org.quiltmc.json5.api.visitor.JsonObjectVisitor;
import org.quiltmc.json5.api.visitor.JsonVisitor;


@ApiStatus.Internal
public interface VisitorWrapper {
	@NotNull VisitorWrapper visitObject(@Nullable String name);
	@NotNull VisitorWrapper visitArray(@Nullable String name);
	void visitString(@Nullable String name, String value);
	void visitBoolean(@Nullable String name, boolean value);
	void visitNumber(@Nullable String name, Number value);
	void visitNull(@Nullable String name);

	static VisitorWrapper create(JsonVisitor visitor) {
		if (visitor instanceof JsonObjectVisitor) {
			return create((JsonObjectVisitor) visitor);
		} else {
			return create((JsonArrayVisitor) visitor);
		}
	}
	static VisitorWrapper create(JsonObjectVisitor visitor) {
		return new ObjectWrapper(visitor);
	}

	static VisitorWrapper create(JsonArrayVisitor visitor) {
		return new ArrayWrapper(visitor);
	}
}
