package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EFileStorage implements Serializable {
	private static final long serialVersionUID = 6250147748818430409L;

	private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
	private static final List<EFileStorage> ALL = new ArrayList<>();

	// ------------------------------

	public static final EFileStorage DISK = new EFileStorage(1, "Disk");
	public static final EFileStorage DATA_BASE = new EFileStorage(2, "DataBase");

	// ------------------------------

	private Integer id;

	// ------------------------------

	public EFileStorage(Integer id, String name) {
		this.id = id;

		ID_TO_NAME.put(id, name);
		ALL.add(this);
	}

	public EFileStorage() {
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_NAME.get(getId());
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EFileStorage)) return false;

		EFileStorage that = (EFileStorage) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);
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

	public static List<EFileStorage> list() {
		return new ArrayList<>(ALL);
	}
}
