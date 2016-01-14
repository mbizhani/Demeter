package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ECalendar implements Serializable {
	public static final ECalendar JALALI = new ECalendar(1);
	public static final ECalendar GREGORIAN = new ECalendar(2);

	private Integer id;

	private ECalendar(Integer id) {
		this.id = id;
	}

	public ECalendar() {
	}

	public Integer getId() {
		return id;
	}

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

	/*TODO
	@Override
	public String toString() {
		return literalToString != null ? literalToString.literalToString(getId()) : getId().toString();
	}*/

	public static List<ECalendar> list() {
		return Arrays.asList(JALALI, GREGORIAN);
	}

}
