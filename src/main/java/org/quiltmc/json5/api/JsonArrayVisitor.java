package org.quiltmc.json5.api;

import org.jetbrains.annotations.NotNull;

public interface JsonArrayVisitor {
	@NotNull JsonObjectVisitor visitObject();
	@NotNull JsonArrayVisitor visitArray();
	void visitString(String string);
	void visitBoolean(boolean bool);
	void visitNumber(Number number);
	void visitNull();
}
