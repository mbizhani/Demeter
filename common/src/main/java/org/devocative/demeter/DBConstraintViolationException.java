package org.devocative.demeter;

public class DBConstraintViolationException extends DemeterException {
	private static final long serialVersionUID = -7910277878181703760L;

	public DBConstraintViolationException(String constraintName) {
		super(DemeterErrorCode.DBConstraintViolation, constraintName);
	}

	public boolean isConstraint(String name) {
		name = name.toLowerCase();
		String constraint = getErrorParameter().toLowerCase();

		return name.equals(constraint) || // HSQLDB
			constraint.contains(name) || // Oracle DB: <SCHEMA>.<CONSTRAINT_NAME>
			name.contains(constraint); // For more precaution :D
	}
}
