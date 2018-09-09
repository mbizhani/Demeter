package org.devocative.demeter.filter;

import org.devocative.adroit.vo.RangeVO;

import java.util.Collection;

public interface IFilterEvent {
	void ifString(String propName, String value, boolean useLike, boolean caseInsensitive);

	void ifRange(String propName, RangeVO value);

	void ifFilterer(String propName, Object value);

	void ifCollection(String propName, Collection col);

	void ifOther(String propName, Object value);
}
