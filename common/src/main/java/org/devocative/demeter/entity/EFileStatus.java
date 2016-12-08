package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EFileStatus implements Serializable {
	private static final long serialVersionUID = 4627911472467658265L;

	private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
	private static final List<EFileStatus> ALL = new ArrayList<>();

	// ------------------------------

	public static final EFileStatus VALID = new EFileStatus(1, "Valid");
	public static final EFileStatus EXPIRED = new EFileStatus(2, "Expired");
	public static final EFileStatus DELETED = new EFileStatus(3, "Deleted");

	// ------------------------------

	private Integer id;

	// ------------------------------

	public EFileStatus(Integer id, String name) {
		this.id = id;

		ID_TO_NAME.put(id, name);
		ALL.add(this);
	}

	public EFileStatus() {
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
		if (!(o instanceof EFileStatus)) return false;

		EFileStatus that = (EFileStatus) o;

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

	public static List<EFileStatus> list() {
		return new ArrayList<>(ALL);
	}
}
