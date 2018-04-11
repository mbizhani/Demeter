package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EDatePatternType {
	P01(1, "yyyy/MM/dd"),
	P02(2, "yyyy-MM-dd"),
	P03(3, "MM/dd/yyyy"),
	P04(4, "MM-dd-yyyy");

	// ------------------------------

	private Integer id;

	private String format;

	// ------------------------------

	EDatePatternType(Integer id, String format) {
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

	public static List<EDatePatternType> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EDatePatternType, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EDatePatternType eDatePatternType) {
			return eDatePatternType != null ? eDatePatternType.getId() : null;
		}

		@Override
		public EDatePatternType convertToEntityAttribute(Integer integer) {
			for (EDatePatternType eDatePatternType : values()) {
				if (eDatePatternType.getId().equals(integer)) {
					return eDatePatternType;
				}
			}
			return null;
		}
	}
}
