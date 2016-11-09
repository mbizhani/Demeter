package org.devocative.demeter.iservice.template;

public interface IToStringConverter<T> {
	String convertToString(T obj);

	IToStringConverter<Object> DEFAULT = new IToStringConverter<Object>() {
		@Override
		public String convertToString(Object obj) {
			return obj != null ? obj.toString() : null;
		}
	};
}
