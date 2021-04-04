package org.quiltmc.json5.impl.wrapper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.JsonArrayVisitor;

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
