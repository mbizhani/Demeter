package org.devocative.demeter.entity;

import java.util.List;

public interface IRoleRowAccess {
	List<Role> getAllowedRoles();

	void setAllowedRoles(List<Role> roles);
}
