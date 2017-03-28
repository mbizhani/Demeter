package org.devocative.demeter;

import org.devocative.demeter.entity.IAuthorizationKey;

public enum DemeterAuthorizationKey implements IAuthorizationKey {
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
