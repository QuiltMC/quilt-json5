package org.quiltmc.json5.parser.wrapper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.api.JsonObjectVisitor;

@ApiStatus.Internal
public final class ObjectWrapper implements VisitorWrapper {
	private final JsonObjectVisitor visitor;

	ObjectWrapper(JsonObjectVisitor visitor) {
		this.visitor = visitor;
	}
	@Override
	public @NotNull VisitorWrapper visitObject(@Nullable String name) {
		return new ObjectWrapper(this.visitor.visitObject(name));
	}

	@Override
	public @NotNull VisitorWrapper visitArray(@Nullable String name) {
		checkKey(name);
		return new ArrayWrapper(this.visitor.visitArray(name));
	}

	@Override
	public void visitString(@Nullable String name, String value) {
		checkKey(name);
		this.visitor.visitString(name, value);
	}

	@Override
	public void visitBoolean(@Nullable String name, boolean value) {
		checkKey(name);
		this.visitor.visitBoolean(name, value);
	}

	@Override
	public void visitNumber(@Nullable String name, Number value) {
		checkKey(name);
		this.visitor.visitNumber(name, value);
	}

	@Override
	public void visitNull(@Nullable String name) {
		checkKey(name);
		this.visitor.visitNull(name);
	}


	private static <T> void checkKey(T in) {
		if (in == null) {
			throw new IllegalStateException("Parser tried to pass a null value to an object");
		}
	}
}
