package org.devocative.demeter;

public class DBConstraintViolationException extends DemeterException {
	private static final long serialVersionUID = -7910277878181703760L;

	public DBConstraintViolationException(String constraintName) {
		super(DemeterErrorCode.DBConstraintViolation, constraintName);
	}

	public String getConstraintName() {
		return getErrorParameter();
	}
}
