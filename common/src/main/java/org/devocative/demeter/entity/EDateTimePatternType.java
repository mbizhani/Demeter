package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EDateTimePatternType {
	P01(1, "yyyy/MM/dd HH:mm:ss"),
	P02(2, "yyyy-MM-dd HH:mm:ss"),
	P03(3, "MM/dd/yyyy HH:mm:ss"),
	P04(4, "MM-dd-yyyy HH:mm:ss");

	// ------------------------------

	private Integer id;

	private String format;

	// ------------------------------

	EDateTimePatternType(Integer id, String format) {
		this.id = id;
		this.format = format;
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getFormat() {
		return format;
	}

	// ---------------

	@Override
	public String toString() {
		return getFormat();
	}

	// ------------------------------

	public static List<EDateTimePatternType> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EDateTimePatternType, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EDateTimePatternType eDateTimePatternType) {
			return eDateTimePatternType != null ? eDateTimePatternType.getId() : null;
		}

		@Override
		public EDateTimePatternType convertToEntityAttribute(Integer integer) {
			for (EDateTimePatternType literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
