package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ERoleMode implements Serializable {
	private static final long serialVersionUID = 6142423391203701806L;

	private static final Map<Integer, ERoleMode> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final ERoleMode NORMAL = new ERoleMode(1, "Normal");
	public static final ERoleMode DYNAMIC = new ERoleMode(2, "Dynamic");
	public static final ERoleMode MAIN = new ERoleMode(3, "Main");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private ERoleMode(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public ERoleMode() {
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_LIT.get(getId()).name;
	}

	// ---------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ERoleMode)) return false;

		ERoleMode eRoleMode = (ERoleMode) o;

		return !(getId() != null ? !getId().equals(eRoleMode.getId()) : eRoleMode.getId() != null);
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ERoleMode> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}

	public static ERoleMode findByName(String name) {
		for (ERoleMode roleMode : ID_TO_LIT.values()) {
			if (roleMode.getName().equals(name)) {
				return roleMode;
			}
		}
		return null;
	}
}
