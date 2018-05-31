package org.devocative.demeter.entity;

import org.devocative.adroit.date.EUniCalendar;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public enum ECalendar {
	PERSIAN(1, "Persian", EUniCalendar.Persian),
	GREGORIAN(2, "Gregorian", EUniCalendar.Gregorian);

	// ------------------------------

	private Integer id;

	private String name;

	private EUniCalendar calendar;

	// ------------------------------

	ECalendar(Integer id, String name, EUniCalendar calendar) {
		this.id = id;
		this.name = name;
		this.calendar = calendar;
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public EUniCalendar getCalendar() {
		return calendar;
	}

	// ---------------

	public String convertToString(Date dt, String pattern, TimeZone timeZone) {
		return calendar.convertToString(dt, pattern, timeZone);
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
