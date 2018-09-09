package org.devocative.demeter.filter;

import org.devocative.adroit.ObjectUtil;
import org.devocative.adroit.vo.RangeVO;

import java.util.Collection;
import java.util.stream.Stream;

class CollectionFilterEvent<T> implements IFilterEvent {
	private Stream<T> stream;

	public CollectionFilterEvent(Collection<T> src) {
		this.stream = src.stream();
	}

	public Stream<T> getStream() {
		return stream;
	}

	@Override
	public void ifString(String propName, String value, boolean useLike, boolean caseInsensitive) {
		stream = stream.filter(t -> {
			Object beanValue = ObjectUtil.getPropertyValue(t, propName, false);

			if (beanValue != null) {
				String beanValueStr = beanValue.toString();
				if (useLike && caseInsensitive) {
					return beanValueStr.toLowerCase().contains(value.toLowerCase());
				} else if (useLike) {
					return beanValueStr.contains(value);
				} else if (caseInsensitive) {
					return beanValueStr.equalsIgnoreCase(value);
				} else {
					return beanValueStr.equals(value);
				}
			}
			return true;
		});
	}

	@Override
	public void ifRange(String propName, RangeVO rangeVO) {
		stream = stream.filter(t -> {
			Object beanValue = ObjectUtil.getPropertyValue(t, propName, false);
			if (beanValue instanceof Comparable) {
				boolean result = true;
				Comparable beanValueComp = (Comparable) beanValue;
				if (rangeVO.getLower() != null) {
					result = beanValueComp.compareTo(rangeVO.getLower()) >= 0;
				}
				if (rangeVO.getUpper() != null && result) {
					result = beanValueComp.compareTo(rangeVO.getUpper()) < 0;
				}
				return result;
			}
			return true;
		});
	}

	@Override
	public void ifFilterer(String propName, Object value) {
		throw new RuntimeException("filterCollection.ifFilterer not implemented");
	}

	@Override
	public void ifCollection(String propName, Collection col) {
		stream = stream.filter(t -> {
			Object beanValue = ObjectUtil.getPropertyValue(t, propName, false);
			if (beanValue != null) {
				if (beanValue instanceof Collection) {
					Collection beanValueCol = (Collection) beanValue;
					return col.containsAll(beanValueCol);
				} else {
					return col.contains(beanValue);
				}
			}
			return true;
		});
	}

	@Override
	public void ifOther(String propName, Object value) {
		stream = stream.filter(t -> {
			Object beanValue = ObjectUtil.getPropertyValue(t, propName, false);
			return beanValue == null || beanValue.equals(value);
		});
	}
}
