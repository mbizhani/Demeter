package org.devocative.demeter.imodule;

public abstract class DModuleException extends RuntimeException {
	private String moduleName;
	private DErrorCode errorCode;
	private String errorParameter;

	protected DModuleException(String moduleName, DErrorCode errorCode, String errorParameter, Throwable cause) {
		super(cause);

		this.moduleName = moduleName;
		this.errorCode = errorCode;
		this.errorParameter = errorParameter;
	}

	public String getErrorParameter() {
		return errorParameter;
	}

	public String getMessage() {
		return String.format("Err.%s.%s", moduleName, errorCode.getCode());
	}

	public String getDefaultDescription() {
		return errorCode.getDefaultDescription();
	}
}
