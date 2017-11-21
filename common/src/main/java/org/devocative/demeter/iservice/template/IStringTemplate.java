package org.devocative.demeter.iservice.template;

import java.util.Map;

public interface IStringTemplate<W> {
	String GROOVY_DELEGATE_KEY = "DELEGATE";

	IStringTemplate setConvertValuesToString(boolean convertValuesToString);

	Object process(Map<String, Object> params);

	W unwrap();

	<T> IStringTemplate registerToStringConverter(Class<T> cls, IToStringConverter<T> converter);
}
