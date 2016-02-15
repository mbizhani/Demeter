package org.devocative.demeter;

import org.devocative.demeter.imodule.DErrorCode;

public enum DemeterErrorCode implements DErrorCode {
	InvalidUser("Invalid username or password");

	private String defaultDescription;

	DemeterErrorCode(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getDefaultDescription() {
		return defaultDescription;
	}
}
