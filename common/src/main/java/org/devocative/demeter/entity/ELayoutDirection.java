package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ELayoutDirection implements Serializable {
	private static final long serialVersionUID = 581360386297611032L;

	private static final Map<Integer, ELayoutDirection> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final ELayoutDirection LTR = new ELayoutDirection(1, "LTR");
	public static final ELayoutDirection RTL = new ELayoutDirection(2, "RTL");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private ELayoutDirection(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public ELayoutDirection() {
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_LIT.get(getId()).name;
	}

	public String getHtmlDir() {
		switch (getId()) {
			case 1:
				return "ltr";
			case 2:
				return "rtl";
		}
		throw new RuntimeException("Invalid Layout Direction: " + getId());
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ELayoutDirection)) return false;

		ELayoutDirection that = (ELayoutDirection) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

		return true;
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

	public static List<ELayoutDirection> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}
}
