package org.devocative.demeter.entity;

import org.devocative.adroit.CalendarUtil;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public enum ECalendar {
	PERSIAN(1, "Persian"),
	GREGORIAN(2, "Gregorian");

	// ------------------------------

	private Integer id;

	private String name;

	// ------------------------------

	ECalendar(Integer id, String name) {
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

	public String convertToString(Date dt, String pattern) {
		switch (this) {
			case PERSIAN:
				return CalendarUtil.toPersian(dt, pattern);

			case GREGORIAN:
				return CalendarUtil.formatDate(dt, pattern);
		}

		return null;
	}

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ECalendar> list() {
		return Arrays.asList(values());
	}

	public static ECalendar findByName(String name) {
		ECalendar result = null;
		for (ECalendar calendar : values()) {
			if (calendar.getName().equals(name)) {
				result = calendar;
				break;
			}
		}
		return result;
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<ECalendar, Integer> {
		@Override
		public Integer convertToDatabaseColumn(ECalendar eCalendar) {
			return eCalendar != null ? eCalendar.getId() : null;
		}

		@Override
		public ECalendar convertToEntityAttribute(Integer integer) {
			for (ECalendar literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
