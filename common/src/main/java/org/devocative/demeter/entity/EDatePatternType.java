package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EDatePatternType implements Serializable {
	private static final Map<Integer, String> FORMATS_MAP = new HashMap<>();
	private static final List<EDatePatternType> LITERALS = new ArrayList<>();

	public static final EDatePatternType P01 = new EDatePatternType(1, "yyyy/MM/dd");
	public static final EDatePatternType P02 = new EDatePatternType(2, "yyyy-MM-dd");
	public static final EDatePatternType P03 = new EDatePatternType(3, "MM/dd/yyyy");
	public static final EDatePatternType P04 = new EDatePatternType(4, "MM-dd-yyyy");

	private Integer id;

	public EDatePatternType() {
	}

	public EDatePatternType(Integer id, String format) {
		this.id = id;
		FORMATS_MAP.put(id, format);
		LITERALS.add(this);
	}

	public Integer getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EDatePatternType)) return false;

		EDatePatternType that = (EDatePatternType) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return FORMATS_MAP.get(id);
	}

	public static List<EDatePatternType> list() {
		return new ArrayList<EDatePatternType>(LITERALS);
	}
}
