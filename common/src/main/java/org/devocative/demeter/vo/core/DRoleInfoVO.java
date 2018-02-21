package org.devocative.demeter.vo.core;

public class DRoleInfoVO {
	private String name;
	private String permissions;

	// ------------------------------

	public DRoleInfoVO(String name, String permissions) {
		this.name = name;
		this.permissions = permissions;
	}

	// ------------------------------

	public String getName() {
		return name;
	}

	public String getPermissions() {
		return permissions;
	}
}
