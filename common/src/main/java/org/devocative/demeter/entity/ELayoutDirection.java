package org.devocative.demeter.entity;

import java.util.Arrays;
import java.util.List;

public enum ELayoutDirection {
	LTR(1, "LTR"),
	RTL(2, "RTL");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	ELayoutDirection(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getHtmlDir() {
		switch (this) {
			case LTR:
				return "ltr";
			case RTL:
				return "rtl";
		}
		throw new RuntimeException("Invalid Layout Direction: " + getId());
	}

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ELayoutDirection> list() {
		return Arrays.asList(values());
	}

	public static ELayoutDirection findByName(String name) {
		ELayoutDirection result = null;
		for (ELayoutDirection direction : values()) {
			if (direction.getName().equals(name)) {
				result = direction;
				break;
			}
		}
		return result;
	}
}
