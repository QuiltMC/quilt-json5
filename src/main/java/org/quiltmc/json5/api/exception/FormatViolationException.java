package org.quiltmc.json5.api.exception;

public class FormatViolationException extends RuntimeException {
	public FormatViolationException() {
	}

	public FormatViolationException(String message) {
		super(message);
	}

	public FormatViolationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FormatViolationException(Throwable cause) {
		super(cause);
	}
}
