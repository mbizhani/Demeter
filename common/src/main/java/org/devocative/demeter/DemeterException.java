package org.devocative.demeter;

import org.devocative.demeter.imodule.DModuleException;

public class DemeterException extends DModuleException {
	private static final long serialVersionUID = -784969764970559551L;

	public DemeterException(DemeterErrorCode errorCode) {
		this(errorCode, null, null);
	}

	public DemeterException(DemeterErrorCode errorCode, String errorParameter) {
		this(errorCode, errorParameter, null);
	}

	// Main Constructor
	public DemeterException(DemeterErrorCode errorCode, String errorParameter, Throwable cause) {
		super("dmt", errorCode, errorParameter, cause);
	}
}
