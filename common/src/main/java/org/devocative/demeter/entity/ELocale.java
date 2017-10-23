package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ELocale implements Serializable {
	private static final long serialVersionUID = 1968359933659360157L;

	private static final Map<Integer, ELocale> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final ELocale FA = new ELocale(1, "Farsi", "fa", ELayoutDirection.RTL, ECalendar.PERSIAN);
	public static final ELocale EN = new ELocale(2, "English", "en", ELayoutDirection.LTR, ECalendar.GREGORIAN);

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	@Transient
	private String code;

	@Transient
	private ELayoutDirection layoutDirection;

	@Transient
	private ECalendar defaultCalendar;

	// ------------------------------

	private ELocale(Integer id, String name, String code, ELayoutDirection layoutDirection, ECalendar defaultCalendar) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.layoutDirection = layoutDirection;
		this.defaultCalendar = defaultCalendar;

		ID_TO_LIT.put(id, this);
	}

	public ELocale() {
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_LIT.get(getId()).name;
	}

	public String getCode() {
		return ID_TO_LIT.get(getId()).code;
	}

	public ELayoutDirection getLayoutDirection() {
		return ID_TO_LIT.get(getId()).layoutDirection;
	}

	public ECalendar getDefaultCalendar() {
		return ID_TO_LIT.get(getId()).defaultCalendar;
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ELocale)) return false;

		ELocale that = (ELocale) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<ELocale> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}

	public static ELocale findByCode(String code) {
		ELocale result = null;
		for (ELocale locale : ID_TO_LIT.values()) {
			if (locale.getCode().equals(code)) {
				result = locale;
				break;
			}
		}
		return result;
	}
}
