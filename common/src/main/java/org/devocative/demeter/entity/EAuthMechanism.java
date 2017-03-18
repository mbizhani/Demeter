package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EAuthMechanism implements Serializable {
	private static final long serialVersionUID = -6830228264271054817L;

	private static final Map<Integer, EAuthMechanism> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final EAuthMechanism DATABASE = new EAuthMechanism(1, "Database");
	public static final EAuthMechanism LDAP = new EAuthMechanism(2, "LDAP");
	public static final EAuthMechanism OTHER = new EAuthMechanism(10, "Other");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private EAuthMechanism(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public EAuthMechanism() {
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
		if (!(o instanceof EAuthMechanism)) return false;

		EAuthMechanism that = (EAuthMechanism) o;

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

	public static List<EAuthMechanism> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}
}
