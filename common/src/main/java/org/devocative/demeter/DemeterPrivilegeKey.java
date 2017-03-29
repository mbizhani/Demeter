package org.devocative.demeter;

import org.devocative.demeter.entity.IPrivilegeKey;

public enum DemeterPrivilegeKey implements IPrivilegeKey {
	RoleAdd, RoleEdit,
	UserAdd, UserEdit,
	PersonAdd, PersonEdit;

	private String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setModule(String module) {
		name = String.format("%s.%s", module, name());
	}
}
