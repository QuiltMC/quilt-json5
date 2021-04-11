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

package org.quiltmc.json5.impl.visitor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.json5.api.visitor.JsonArrayVisitor;
import org.quiltmc.json5.api.JsonNull;
import org.quiltmc.json5.api.visitor.JsonObjectVisitor;
import org.quiltmc.json5.api.visitor.JsonVisitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
@ApiStatus.Internal
public final class TreeVisitor implements JsonVisitor {
	public Object root;
	@Override
	public JsonObjectVisitor rootObject() {
		root = new LinkedHashMap();
		return new ObjVisitor((LinkedHashMap) root);
	}

	@Override
	public JsonArrayVisitor rootArray() {
		return null;
	}

	@Override
	public void rootString(String string) {
		root = string;
	}

	@Override
	public void rootBoolean(boolean bool) {
		root = bool;
	}

	@Override
	public void rootNumber(Number number) {
		root = number;
	}

	@Override
	public void rootNull() {
		root = JsonNull.INSTANCE;
	}

	private static final class ObjVisitor implements JsonObjectVisitor {
		private final LinkedHashMap parent;
		ObjVisitor(LinkedHashMap parent) {
			this.parent = parent;
		}
		@Override
		public @NotNull JsonObjectVisitor visitObject(String name) {
			LinkedHashMap ret = new LinkedHashMap();
			parent.put(name, ret);
			return new ObjVisitor(ret);
		}

		@Override
		public @NotNull JsonArrayVisitor visitArray(String name) {
			List ret = new ArrayList();
			parent.put(name, ret);
			return new ArrayVisitor(ret);
		}

		@Override
		public void visitString(String name, String value) {
			parent.put(name, value);
		}

		@Override
		public void visitBoolean(String name, boolean value) {
			parent.put(name, value);
		}

		@Override
		public void visitNumber(String name, Number value) {
			parent.put(name, value);
		}

		@Override
		public void visitNull(String name) {
			parent.put(name, JsonNull.INSTANCE);
		}
	}

	private static final class ArrayVisitor implements JsonArrayVisitor {
		private final List parent;
		ArrayVisitor(List parent) {
			this.parent = parent;
		}

		@Override
		public @NotNull JsonObjectVisitor visitObject() {
			LinkedHashMap ret = new LinkedHashMap();
			parent.add(ret);
			return new ObjVisitor(ret);
		}

		@Override
		public @NotNull JsonArrayVisitor visitArray() {
			List ret = new ArrayList();
			parent.add( ret);
			return new ArrayVisitor(ret);
		}

		@Override
		public void visitString(String string) {
			parent.add(string);
		}

		@Override
		public void visitBoolean(boolean bool) {
			parent.add(bool);
		}

		@Override
		public void visitNumber(Number number) {
			parent.add(number);
		}

		@Override
		public void visitNull() {
			parent.add(JsonNull.INSTANCE);
		}
	}
}
