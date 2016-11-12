package org.devocative.demeter.iservice.template;

import java.util.Map;

public interface IStringTemplate {
	IStringTemplate setConvertValuesToString(boolean convertValuesToString);

	String process(Map<String, Object> params);

	Object unwrap();

	<T> IStringTemplate registerToStringConverter(Class<T> cls, IToStringConverter<T> converter);
}
