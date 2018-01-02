package org.devocative.demeter.filter;

import org.devocative.adroit.ObjectUtil;
import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.iservice.persistor.FilterOption;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionUtil {
	public static void filter(IFilterEvent filterEvent, Object filter, String... ignoreProperties) {
		PropertyDescriptor[] descriptors = ObjectUtil.getPropertyDescriptors(filter, false);
		List<String> ignorePropsList = new ArrayList<>();
		ignorePropsList.add("class");
		Collections.addAll(ignorePropsList, ignoreProperties);

		for (PropertyDescriptor descriptor : descriptors) {
			String propName = descriptor.getName();
			Method readMethod = descriptor.getReadMethod();
			FilterOption search = findAnnotation(FilterOption.class, filter, descriptor);
			if (search != null && search.property().length() > 0) {
				propName = search.property();
			}

			if (ignorePropsList.contains(propName)) {
				continue;
			}

			try {
				Object value = readMethod.invoke(filter);

				if (value == null) {
					continue;
				}

				// ---------- Property: String
				if (value instanceof String) {
					filterEvent.ifString(propName, (String) value, search == null || search.useLike());
				}

				// ---------- Property: RangeVO
				else if (value instanceof RangeVO) {
					filterEvent.ifRange(propName, (RangeVO) value);
				}

				// ---------- Property: an object of Filterer
				else if (value.getClass().isAnnotationPresent(Filterer.class)) {
					filterEvent.ifFilterer(propName, value);
				}

				// ---------- Property: Collection
				else if (value instanceof Collection) {
					Collection col = (Collection) value;
					if (col.size() > 0) {
						filterEvent.ifCollection(propName, col);
					}
				}
				// ---------- Property: other primitive types
				else {
					filterEvent.ifOther(propName, value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static <T> List<T> filterCollection(Collection<T> src, Object filter, String... ignoreProperties) {
		CollectionFilterEvent<T> filterEvent = new CollectionFilterEvent<>(src);
		filter(filterEvent, filter, ignoreProperties);
		return filterEvent.getStream().collect(Collectors.toList());
	}

	// ------------------------------

	// ------------------------------

	private static <T extends Annotation> T findAnnotation(Class<? extends Annotation> annot, Object obj,
														   PropertyDescriptor propertyDescriptor) {
		T result = null;
		Method readMethod = propertyDescriptor.getReadMethod();
		if (readMethod != null)
			result = (T) readMethod.getAnnotation(annot);

		if (result == null) {
			try {
				Field field = obj.getClass().getDeclaredField(propertyDescriptor.getName());
				result = (T) field.getAnnotation(annot);
			} catch (NoSuchFieldException e) {
			}
		}
		return result;
	}

	// ------------------------------

}
