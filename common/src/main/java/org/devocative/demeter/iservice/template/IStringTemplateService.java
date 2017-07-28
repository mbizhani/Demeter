package org.devocative.demeter.iservice.template;

public interface IStringTemplateService {
	String CACHE_KEY = "DMT_STRING_TEMPLATE";

	IStringTemplate create(String template, TemplateEngineType templateEngineType);

	IStringTemplate create(String id, String template, TemplateEngineType engineType);

	void clearCacheFor(String id);
}
