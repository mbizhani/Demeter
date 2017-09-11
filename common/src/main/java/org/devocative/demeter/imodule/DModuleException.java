package org.devocative.demeter.imodule;

public abstract class DModuleException extends RuntimeException {
	private static final long serialVersionUID = 7282948088940717726L;

	private String moduleName;
	private DErrorCode errorCode;
	private String errorParameter;

	protected DModuleException(String moduleName, DErrorCode errorCode, String errorParameter, Throwable cause) {
		super(cause);

		this.moduleName = moduleName;
		this.errorCode = errorCode;
		this.errorParameter = errorParameter;
	}

	public DErrorCode getErrorCode() {
		return errorCode;
	}

	public String getErrorParameter() {
		return errorParameter;
	}

	public String getMessage() {
		if (errorParameter != null) {
			return String.format("%s.%s: %s", moduleName, errorCode.getCode(), errorParameter);
		}
		return String.format("%s.%s", moduleName, errorCode.getCode());
	}

	public String getResourceKey() {
		return String.format("err.%s.%s", moduleName, errorCode.getCode());
	}

	public String getDefaultDescription() {
		return errorCode.getDefaultDescription();
	}
}
