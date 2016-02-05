package org.devocative.demeter;

public class DSystemException extends RuntimeException {

	public DSystemException(String message) {
		this(message, null);
	}

	public DSystemException(String message, Throwable cause) {
		super(message, cause);
	}
}
