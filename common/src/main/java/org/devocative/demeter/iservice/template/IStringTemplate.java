package org.devocative.demeter.iservice.template;

import java.util.Map;

public interface IStringTemplate {
	String process(Map<String, Object> params);
}
