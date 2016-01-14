package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ELayoutDirection implements Serializable {
	public static final ELayoutDirection LTR = new ELayoutDirection(1);
	public static final ELayoutDirection RTL = new ELayoutDirection(2);

	private Integer id;

	private ELayoutDirection(Integer id) {
		this.id = id;
	}

	public ELayoutDirection() {
	}

	public Integer getId() {
		return id;
	}

	public String getHtmlDir() {
		switch (getId()) {
			case 1:
				return "ltr";
			case 2:
				return "rtl";
		}
		throw new RuntimeException("Invalid Layout Direction: " + getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ELayoutDirection)) return false;

		ELayoutDirection that = (ELayoutDirection) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	/* TODO
	@Override
	public String toString() {
		return literalToString != null ? literalToString.literalToString(getId()) : getId().toString();
	}*/

	public static List<ELayoutDirection> list() {
		return Arrays.asList(LTR, RTL);
	}
}
