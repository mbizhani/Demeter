package org.devocative.demeter.iservice.template;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseStringTemplate implements IStringTemplate {
	private Map<Class, IToStringConverter> map = new HashMap<>();

	protected boolean convertValuesToString = false;

	// ------------------------------

	@Override
	public IStringTemplate setConvertValuesToString(boolean convertValuesToString) {
		this.convertValuesToString = convertValuesToString;
		return this;
	}

	@Override
	public <T> IStringTemplate registerToStringConverter(Class<T> cls, IToStringConverter<T> converter) {
		map.put(cls, converter);
		return this;
	}

	// ------------------------------

	protected Map<String, Object> convertValuesToString(Map<String, Object> data) {
		Map<String, Object> result = new HashMap<>();
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			if (entry.getValue() instanceof Map) {
				result.put(entry.getKey(), convertValuesToString((Map<String, Object>) entry.getValue()));
			} else {
				result.put(entry.getKey(), findAndConvert(entry.getValue()));
			}
		}
		return result;
	}

	// ------------------------------

	private String findAndConvert(Object obj) {
		IToStringConverter converter = IToStringConverter.DEFAULT;
		for (Map.Entry<Class, IToStringConverter> entry : map.entrySet()) {
			if (entry.getKey().isInstance(obj)) {
				converter = entry.getValue();
				break;
			}
		}

		return converter.convertToString(obj);
	}
}
