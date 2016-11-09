package org.devocative.demeter.iservice.template;

import java.util.Map;

public interface IStringTemplate {
	String process(Map<String, Object> params, boolean convertValuesToString);

	Object unwrap();

	<T> void registerToStringConverter(Class<T> cls, IToStringConverter<T> converter);
}
