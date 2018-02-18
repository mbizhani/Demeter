package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;

public class ERowMod implements Serializable {
	private static final long serialVersionUID = -3933153565923626628L;

	private static final Map<Integer, ERowMod> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final int DELETED_ID = -1;
	public static final int NORMAL_ID = 1;
	public static final int ROLE_ID = 2;

	public static final int SYSTEM_ID = 10;
	public static final int ROOT_ID = 11;
	public static final int ADMIN_ID = 12;
	public static final int CREATOR_ID = 13;

	// ---------------

	public static final ERowMod DELETED = new ERowMod(DELETED_ID, "Deleted");
	public static final ERowMod NORMAL = new ERowMod(NORMAL_ID, "Normal");
	public static final ERowMod ROLE = new ERowMod(ROLE_ID, "Role");

	public static final ERowMod SYSTEM = new ERowMod(SYSTEM_ID, "System");
	public static final ERowMod ROOT = new ERowMod(ROOT_ID, "Root");
	public static final ERowMod ADMIN = new ERowMod(ADMIN_ID, "Admin");
	public static final ERowMod CREATOR = new ERowMod(CREATOR_ID, "Creator");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private ERowMod(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public ERowMod() {
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
		if (!(o instanceof ERowMod)) return false;

		ERowMod eRowMod = (ERowMod) o;

		return !(getId() != null ? !getId().equals(eRowMod.getId()) : eRowMod.getId() != null);
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

	public static List<ERowMod> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}

	public static List<ERowMod> notDeleted() {
		List<ERowMod> list = list();
		list.remove(DELETED);
		return list;
	}

	public static List<ERowMod> accessList() {
		return Arrays.asList(ROOT, ADMIN, CREATOR, NORMAL);
	}
}
