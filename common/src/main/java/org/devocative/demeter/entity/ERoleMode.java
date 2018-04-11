package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum ERoleMode {
	NORMAL(1, "Normal"),
	DYNAMIC(2, "Dynamic"),
	MAIN(3, "Main");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	ERoleMode(Integer id, String name) {
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

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ERoleMode> list() {
		return Arrays.asList(values());
	}

	public static ERoleMode findByName(String name) {
		for (ERoleMode roleMode : values()) {
			if (roleMode.getName().equals(name)) {
				return roleMode;
			}
		}
		return null;
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<ERoleMode, Integer> {
		@Override
		public Integer convertToDatabaseColumn(ERoleMode eRoleMode) {
			return eRoleMode != null ? eRoleMode.getId() : null;
		}

		@Override
		public ERoleMode convertToEntityAttribute(Integer integer) {
			for (ERoleMode literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
