package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EUserStatus {
	ENABLED(1, "Enabled"),
	DISABLED(2, "Disabled"),
	LOCKED(3, "Locked");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	EUserStatus(Integer id, String name) {
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

	public static List<EUserStatus> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EUserStatus, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EUserStatus eUserStatus) {
			return eUserStatus != null ? eUserStatus.getId() : null;
		}

		@Override
		public EUserStatus convertToEntityAttribute(Integer integer) {
			for (EUserStatus literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
