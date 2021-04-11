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

package org.quiltmc.json5.api.visitor;

import org.jetbrains.annotations.NotNull;

public interface JsonObjectVisitor extends JsonVisitor {
	@Override
	default JsonObjectVisitor rootObject() {
		return this;
	}

	/**
	 * Begins reading a json object. The first object is implicit.
	 */
	@NotNull JsonObjectVisitor visitObject(String name);
	@NotNull JsonArrayVisitor visitArray(String name);
	void visitString(String name, String value);
	void visitBoolean(String name, boolean value);
	void visitNumber(String name, Number value);
	void visitNull(String name);
}
