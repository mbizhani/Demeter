package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ERowMod implements Serializable {
	private static final long serialVersionUID = -3933153565923626628L;

	private static final Map<Integer, ERowMod> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final ERowMod DELETED = new ERowMod(-1, "Deleted");
	public static final ERowMod NORMAL = new ERowMod(1, "Normal");
	public static final ERowMod SYSTEM = new ERowMod(10, "System");
	public static final ERowMod ROOT = new ERowMod(11, "Root");
	public static final ERowMod ADMIN = new ERowMod(12, "Admin");
	public static final ERowMod CREATOR = new ERowMod(13, "Creator");

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

	// ------------------------------

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
}
