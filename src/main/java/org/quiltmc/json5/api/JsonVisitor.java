package org.quiltmc.json5.api;

import org.quiltmc.json5.api.exception.FormatViolationException;

public interface JsonVisitor {
	default JsonObjectVisitor rootObject() {
		throw new FormatViolationException();
	}
	default JsonArrayVisitor rootArray() {
		throw new FormatViolationException();
	}

	default void rootString(String string) {
		throw new FormatViolationException();
	}

	default void rootBoolean(boolean bool) {
		throw new FormatViolationException();
	}

	default void rootNumber(Number number) {
		throw new FormatViolationException();
	}

	default void rootNull() {
		throw new FormatViolationException();
	}
}
