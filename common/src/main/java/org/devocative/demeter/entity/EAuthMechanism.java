package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EAuthMechanism {
	DATABASE(1, "Database"),
	LDAP(2, "LDAP"),
	OTHER(10, "Other");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	EAuthMechanism(Integer id, String name) {
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

	public static List<EAuthMechanism> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EAuthMechanism, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EAuthMechanism eAuthMechanism) {
			return eAuthMechanism != null ? eAuthMechanism.getId() : null;
		}

		@Override
		public EAuthMechanism convertToEntityAttribute(Integer integer) {
			for (EAuthMechanism literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
