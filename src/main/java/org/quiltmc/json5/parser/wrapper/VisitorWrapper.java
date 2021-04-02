package org.quiltmc.json5.parser.wrapper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.JsonArrayVisitor;
import org.quiltmc.json5.api.JsonObjectVisitor;
@ApiStatus.Internal
public interface VisitorWrapper {
	@NotNull VisitorWrapper visitObject(@Nullable String name);
	@NotNull VisitorWrapper visitArray(@Nullable String name);
	void visitString(@Nullable String name, String value);
	void visitBoolean(@Nullable String name, boolean value);
	void visitNumber(@Nullable String name, Number value);
	void visitNull(@Nullable String name);

	static VisitorWrapper create(JsonObjectVisitor visitor) {
		return new ObjectWrapper(visitor);
	}

	static VisitorWrapper create(JsonArrayVisitor visitor) {
		return new ArrayWrapper(visitor);
	}
}
