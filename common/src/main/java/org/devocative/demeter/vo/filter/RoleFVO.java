//overwrite
package org.devocative.demeter.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.entity.ERoleMode;
import org.devocative.demeter.entity.ERowMode;
import org.devocative.demeter.entity.Privilege;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class RoleFVO implements Serializable {
	private static final long serialVersionUID = -1494627878L;

	private String name;
	private List<ERoleMode> roleMode;
	private List<Privilege> permissions;
	private List<Privilege> denials;
	private List<ERowMode> rowMode;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;
	private RangeVO<Date> modificationDate;
	private List<User> modifierUser;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ERoleMode> getRoleMode() {
		return roleMode;
	}

	public void setRoleMode(List<ERoleMode> roleMode) {
		this.roleMode = roleMode;
	}

	public List<Privilege> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Privilege> permissions) {
		this.permissions = permissions;
	}

	public List<Privilege> getDenials() {
		return denials;
	}

	public void setDenials(List<Privilege> denials) {
		this.denials = denials;
	}

	public List<ERowMode> getRowMode() {
		return rowMode;
	}

	public void setRowMode(List<ERowMode> rowMode) {
		this.rowMode = rowMode;
	}

	public RangeVO<Date> getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(RangeVO<Date> creationDate) {
		this.creationDate = creationDate;
	}

	public List<User> getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(List<User> creatorUser) {
		this.creatorUser = creatorUser;
	}

	public RangeVO<Date> getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(RangeVO<Date> modificationDate) {
		this.modificationDate = modificationDate;
	}

	public List<User> getModifierUser() {
		return modifierUser;
	}

	public void setModifierUser(List<User> modifierUser) {
		this.modifierUser = modifierUser;
	}

}