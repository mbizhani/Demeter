package org.devocative.demeter.iservice.template;

public interface IStringTemplateService {
	IStringTemplate create(String template, TemplateEngineType templateEngineType);

	IStringTemplate create(String id, String template, TemplateEngineType engineType);

	void clearCacheFor(String id);
}
