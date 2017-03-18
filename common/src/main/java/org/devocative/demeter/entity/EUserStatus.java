package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EUserStatus implements Serializable {
	private static final long serialVersionUID = -5776208676258016175L;

	private static final Map<Integer, EUserStatus> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final EUserStatus ENABLED = new EUserStatus(1, "Enabled");
	public static final EUserStatus DISABLED = new EUserStatus(2, "Disabled");
	public static final EUserStatus LOCKED = new EUserStatus(3, "Locked");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private EUserStatus(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public EUserStatus() {
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
		if (!(o instanceof EUserStatus)) return false;

		EUserStatus that = (EUserStatus) o;

		return !(
			getId() != null ? !getId().equals(that.getId()) : that.getId() != null
		);

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

	public static List<EUserStatus> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}
}
