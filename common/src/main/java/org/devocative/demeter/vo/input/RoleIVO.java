package org.devocative.demeter.vo.input;

import org.devocative.demeter.entity.ERoleMode;
import org.devocative.demeter.entity.ERowMode;
import org.devocative.demeter.entity.Privilege;
import org.devocative.demeter.entity.Role;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class RoleIVO implements Serializable {
	private static final long serialVersionUID = -2218762842795470986L;

	private Long id;
	private String name;
	private ERoleMode roleMode;
	private List<Privilege> permissions;
	private List<Privilege> denials;
	private ERowMode rowMode;
	private Date creationDate;
	private Long creatorUserId;
	private Date modificationDate;
	private Long modifierUserId;
	private Integer version;

	// ------------------------------

	public RoleIVO() {
	}

	public RoleIVO(Role ent) {
		fromRole(ent);
	}

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ERoleMode getRoleMode() {
		return roleMode;
	}

	public void setRoleMode(ERoleMode roleMode) {
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

	public ERowMode getRowMode() {
		return rowMode;
	}

	public void setRowMode(ERowMode rowMode) {
		this.rowMode = rowMode;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getCreatorUserId() {
		return creatorUserId;
	}

	public void setCreatorUserId(Long creatorUserId) {
		this.creatorUserId = creatorUserId;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public Long getModifierUserId() {
		return modifierUserId;
	}

	public void setModifierUserId(Long modifierUserId) {
		this.modifierUserId = modifierUserId;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	// ---------------

	public Role toRole() {
		Role ent = new Role();
		ent.setId(getId());
		ent.setName(getName());
		ent.setRoleMode(getRoleMode());
		ent.setPermissions(getPermissions());
		ent.setDenials(getDenials());

		ent.setCreationDate(getCreationDate());
		ent.setCreatorUserId(getCreatorUserId());
		ent.setModificationDate(getModificationDate());
		ent.setModifierUserId(getModifierUserId());
		ent.setVersion(getVersion());
		return ent;
	}

	public void fromRole(Role ent) {
		setId(ent.getId());
		setName(ent.getName());
		setRoleMode(ent.getRoleMode());
		setPermissions(ent.getPermissions());
		setDenials(ent.getDenials());

		setCreationDate(ent.getCreationDate());
		setCreatorUserId(ent.getCreatorUserId());
		setModificationDate(ent.getModificationDate());
		setModifierUserId(ent.getModifierUserId());
		setVersion(ent.getVersion());
	}
}
