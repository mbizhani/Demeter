package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EAuthMechanism implements Serializable {
	private static final List<EAuthMechanism> LITERALS = new ArrayList<>();

	// ------------------------------

	public static final EAuthMechanism DATABASE = new EAuthMechanism(1);
	public static final EAuthMechanism LDAP = new EAuthMechanism(2);
	public static final EAuthMechanism OTHER = new EAuthMechanism(10);

	// ------------------------------

	private Integer id;

	// ------------------------------

	public EAuthMechanism() {
	}

	public EAuthMechanism(Integer id) {
		this.id = id;
		LITERALS.add(this);
	}

	// ------------------------------

	public Integer getId() {
		return id;
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

	// ------------------------------

	public static List<EAuthMechanism> list() {
		return new ArrayList<>(LITERALS);
	}
}
