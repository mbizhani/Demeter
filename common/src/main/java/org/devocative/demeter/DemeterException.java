package org.devocative.demeter;

import org.devocative.demeter.imodule.DModuleException;

public class DemeterException extends DModuleException {

	public DemeterException(DemeterErrorCode errorCode) {
		this(errorCode, null, null);
	}

	public DemeterException(DemeterErrorCode errorCode, String errorParameter) {
		this(errorCode, errorParameter, null);
	}

	// Main Constructor
	public DemeterException(DemeterErrorCode errorCode, String errorParameter, Throwable cause) {
		super("DMT", errorCode, errorParameter, cause);
	}
}
