package org.devocative.demeter.iservice.template;

import java.util.Map;

public interface IStringTemplate<W> {
	IStringTemplate setConvertValuesToString(boolean convertValuesToString);

	Object process(Map<String, Object> params);

	W unwrap();

	<T> IStringTemplate registerToStringConverter(Class<T> cls, IToStringConverter<T> converter);
}
