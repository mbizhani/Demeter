package org.devocative.demeter;

import org.devocative.demeter.imodule.DErrorCode;

public enum DemeterErrorCode implements DErrorCode {
	InvalidUser("Invalid username or password"),
	InvalidCurrentPassword("Invalid current password"),
	UserLocked("User is locked"),
	UserDisabled("User is disabled"),
	DBConstraintViolation("DB Constraint Violated"),

	DuplicateUsername("Duplicate Username"),
	DuplicateRoleName("Duplicate Role Name"),
	NoMainRoleForUser("Current user must have at least one main role for this entity");

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
