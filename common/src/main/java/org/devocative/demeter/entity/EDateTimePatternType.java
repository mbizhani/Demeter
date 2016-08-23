package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EDateTimePatternType implements Serializable {
	private static final long serialVersionUID = 3708198951457435525L;

	private static final Map<Integer, String> FORMATS_MAP = new HashMap<>();
	private static final List<EDateTimePatternType> LITERALS = new ArrayList<>();

	public static final EDateTimePatternType P01 = new EDateTimePatternType(1, "yyyy/MM/dd HH:mm:ss");
	public static final EDateTimePatternType P02 = new EDateTimePatternType(2, "yyyy-MM-dd HH:mm:ss");
	public static final EDateTimePatternType P03 = new EDateTimePatternType(3, "MM/dd/yyyy HH:mm:ss");
	public static final EDateTimePatternType P04 = new EDateTimePatternType(4, "MM-dd-yyyy HH:mm:ss");


	private Integer id;

	public EDateTimePatternType() {
	}

	public EDateTimePatternType(Integer id, String format) {
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
		if (!(o instanceof EDateTimePatternType)) return false;

		EDateTimePatternType that = (EDateTimePatternType) o;

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

	public static List<EDateTimePatternType> list() {
		return new ArrayList<EDateTimePatternType>(LITERALS);
	}

}
