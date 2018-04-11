package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EFileStatus {
	VALID(1, "Valid"),
	EXPIRED(2, "Expired"),
	DELETED(3, "Deleted");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	EFileStatus(Integer id, String name) {
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

	public static List<EFileStatus> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EFileStatus, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EFileStatus eFileStatus) {
			return eFileStatus != null ? eFileStatus.getId() : null;
		}

		@Override
		public EFileStatus convertToEntityAttribute(Integer integer) {
			for (EFileStatus literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
