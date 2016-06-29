package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EUserStatus implements Serializable {
	private static final List<EUserStatus> LITERALS = new ArrayList<>();

	// ------------------------------

	public static final EUserStatus ENABLED = new EUserStatus(1);
	public static final EUserStatus DISABLED = new EUserStatus(2);
	public static final EUserStatus LOCKED = new EUserStatus(3);

	// ------------------------------

	private Integer id;

	// ------------------------------

	public EUserStatus() {
	}

	public EUserStatus(Integer id) {
		this.id = id;
		LITERALS.add(this);
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EUserStatus)) return false;

		EUserStatus that = (EUserStatus) o;

		return !(
			getId() != null ? !getId().equals(that.getId()) : that.getId() != null
		);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	// ------------------------------

	/* TODO
	@Override
	public String toString() {
		switch (getId()) {
			case 1:
				return I18NUtil.getString("EUserStatus.ENABLED", "[Enabled]");

			case 2:
				return I18NUtil.getString("EUserStatus.DISABLED", "[Disabled]");

			case 3:
				return I18NUtil.getString("EUserStatus.LOCKED", "[Locked]");

			default:
				return "[?]";
		}
	}*/

	public static List<EUserStatus> list() {
		return new ArrayList<>(LITERALS);
	}
}
