package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum ERowMode {
	/*
	public static final int DELETED_ID = -1;
	public static final int NORMAL_ID = 1;
	public static final int ROLE_ID = 2;

	public static final int SYSTEM_ID = 10;
	public static final int ROOT_ID = 11;
	public static final int ADMIN_ID = 12;
	public static final int CREATOR_ID = 13;
	*/

	DELETED(-1, "Deleted"),
	NORMAL(1, "Normal"),
	ROLE(2, "Role"),

	SYSTEM(10, "System"),
	ROOT(11, "Root"),
	ADMIN(12, "Admin"),
	CREATOR(13, "Creator");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	ERowMode(Integer id, String name) {
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

	public static List<ERowMode> list() {
		return Arrays.asList(values());
	}

	public static List<ERowMode> notDeleted() {
		List<ERowMode> list = list();
		list.remove(DELETED);
		return list;
	}

	public static List<ERowMode> accessList() {
		return Arrays.asList(ROOT, ADMIN, CREATOR, NORMAL);
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<ERowMode, Integer> {
		@Override
		public Integer convertToDatabaseColumn(ERowMode eRowMode) {
			return eRowMode != null ? eRowMode.getId() : null;
		}

		@Override
		public ERowMode convertToEntityAttribute(Integer integer) {
			for (ERowMode literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
