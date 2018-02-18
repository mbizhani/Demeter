package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;

public class ERowMode implements Serializable {
	private static final long serialVersionUID = -3933153565923626628L;

	private static final Map<Integer, ERowMode> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final int DELETED_ID = -1;
	public static final int NORMAL_ID = 1;
	public static final int ROLE_ID = 2;

	public static final int SYSTEM_ID = 10;
	public static final int ROOT_ID = 11;
	public static final int ADMIN_ID = 12;
	public static final int CREATOR_ID = 13;

	// ---------------

	public static final ERowMode DELETED = new ERowMode(DELETED_ID, "Deleted");
	public static final ERowMode NORMAL = new ERowMode(NORMAL_ID, "Normal");
	public static final ERowMode ROLE = new ERowMode(ROLE_ID, "Role");

	public static final ERowMode SYSTEM = new ERowMode(SYSTEM_ID, "System");
	public static final ERowMode ROOT = new ERowMode(ROOT_ID, "Root");
	public static final ERowMode ADMIN = new ERowMode(ADMIN_ID, "Admin");
	public static final ERowMode CREATOR = new ERowMode(CREATOR_ID, "Creator");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private ERowMode(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public ERowMode() {
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
		if (!(o instanceof ERowMode)) return false;

		ERowMode eRowMode = (ERowMode) o;

		return !(getId() != null ? !getId().equals(eRowMode.getId()) : eRowMode.getId() != null);
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

	public static List<ERowMode> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}

	public static List<ERowMode> notDeleted() {
		List<ERowMode> list = list();
		list.remove(DELETED);
		return list;
	}

	public static List<ERowMode> accessList() {
		return Arrays.asList(ROOT, ADMIN, CREATOR, NORMAL);
	}
}
