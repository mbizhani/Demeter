package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EFileStorage {
	DISK(1, "Disk"),
	DATA_BASE(2, "DataBase");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	EFileStorage(Integer id, String name) {
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

	public static List<EFileStorage> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EFileStorage, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EFileStorage eFileStorage) {
			return eFileStorage != null ? eFileStorage.getId() : null;
		}

		@Override
		public EFileStorage convertToEntityAttribute(Integer integer) {
			for (EFileStorage literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
