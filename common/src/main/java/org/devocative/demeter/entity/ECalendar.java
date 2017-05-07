package org.devocative.demeter.entity;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ECalendar implements Serializable {
	private static final long serialVersionUID = -8563152792867832546L;

	private static final Map<Integer, ECalendar> ID_TO_LIT = new LinkedHashMap<>();

	// ------------------------------

	public static final ECalendar JALALI = new ECalendar(1, "Jalali");
	public static final ECalendar GREGORIAN = new ECalendar(2, "Gregorian");

	// ------------------------------

	private Integer id;

	@Transient
	private String name;

	// ------------------------------

	private ECalendar(Integer id, String name) {
		this.id = id;
		this.name = name;

		ID_TO_LIT.put(id, this);
	}

	public ECalendar() {
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_LIT.get(getId()).name;
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ECalendar)) return false;

		ECalendar eCalendar = (ECalendar) o;

		if (getId() != null ? !getId().equals(eCalendar.getId()) : eCalendar.getId() != null) return false;

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

	public static List<ECalendar> list() {
		return new ArrayList<>(ID_TO_LIT.values());
	}

	public static ECalendar findByName(String name) {
		ECalendar result = null;
		for (ECalendar calendar : ID_TO_LIT.values()) {
			if (calendar.getName().equals(name)) {
				result = calendar;
				break;
			}
		}
		return result;
	}
}
