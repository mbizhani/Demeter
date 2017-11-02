package org.devocative.demeter;

public class DSystemException extends RuntimeException {
	private static final long serialVersionUID = -6990004284474706991L;

	public DSystemException(String message) {
		this(message, null);
	}

	public DSystemException(String message, Throwable cause) {
		super(message, cause);
	}

	public DSystemException(Throwable cause) {
		super(cause);
	}
}
