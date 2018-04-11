package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum ELocale {
	FA(1, "Farsi", "fa", ELayoutDirection.RTL, ECalendar.PERSIAN),
	EN(2, "English", "en", ELayoutDirection.LTR, ECalendar.GREGORIAN);

	// ------------------------------

	private Integer id;

	private String name;

	private String code;

	private ELayoutDirection layoutDirection;

	private ECalendar defaultCalendar;

	// ------------------------------

	ELocale(Integer id, String name, String code, ELayoutDirection layoutDirection, ECalendar defaultCalendar) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.layoutDirection = layoutDirection;
		this.defaultCalendar = defaultCalendar;
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public ELayoutDirection getLayoutDirection() {
		return layoutDirection;
	}

	public ECalendar getDefaultCalendar() {
		return defaultCalendar;
	}

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ELocale> list() {
		return Arrays.asList(values());
	}

	public static ELocale findByCode(String code) {
		ELocale result = null;
		for (ELocale locale : values()) {
			if (locale.getCode().equals(code)) {
				result = locale;
				break;
			}
		}
		return result;
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<ELocale, Integer> {
		@Override
		public Integer convertToDatabaseColumn(ELocale eLocale) {
			return eLocale != null ? eLocale.getId() : null;
		}

		@Override
		public ELocale convertToEntityAttribute(Integer integer) {
			for (ELocale literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
