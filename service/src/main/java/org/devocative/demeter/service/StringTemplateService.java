package org.devocative.demeter.service;

import freemarker.template.*;
import groovy.lang.GroovyShell;
import groovy.text.SimpleTemplateEngine;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.cache.ICache;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.iservice.ICacheService;
import org.devocative.demeter.iservice.template.IStringTemplate;
import org.devocative.demeter.iservice.template.IStringTemplateService;
import org.devocative.demeter.iservice.template.TemplateEngineType;
import org.devocative.demeter.service.template.FreeMarkerStringTemplate;
import org.devocative.demeter.service.template.GroovyStringTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("dmtStringTemplateService")
public class StringTemplateService implements IStringTemplateService {
	private static final Logger logger = LoggerFactory.getLogger(StringTemplateService.class);

	private Configuration freeMarkerCfg;
	private ICache<String, IStringTemplate> templateCache;

	private GroovyShell groovyShell;
	private SimpleTemplateEngine simpleTemplateEngine;

	@Autowired
	private ICacheService cacheService;

	// ------------------------------

	@PostConstruct
	public void initTemplateService() {
		freeMarkerCfg = new Configuration(Configuration.VERSION_2_3_23);
		freeMarkerCfg.setObjectWrapper(new CaseInsensitiveVariableWrapper());

		groovyShell = new GroovyShell();
		simpleTemplateEngine = new SimpleTemplateEngine();
	}

	// ------------------------------

	@Override
	public IStringTemplate create(String template, TemplateEngineType engineType) {
		String id = UUID.nameUUIDFromBytes(template.getBytes()).toString();
		return create(id, template, engineType);
	}

	@Override
	public IStringTemplate create(String id, String template, TemplateEngineType engineType) {
		if (ConfigUtil.getBoolean(DemeterConfigKey.StringTemplateCacheEnabled)) {
			if (getTemplateCache().containsKey(id)) {
				return getTemplateCache().get(id);
			}
		}

		IStringTemplate result;

		switch (engineType) {

			case FreeMarker:
				result = createFreeMarker(id, template);
				break;

			case Groovy:
				result = createGroovy(template);
				break;

			case GroovyShell:
				result = createGroovyShell(template);
				break;

			default:
				throw new RuntimeException("No TemplateEngineType Defined!"); //TODO
		}

		if (ConfigUtil.getBoolean(DemeterConfigKey.StringTemplateCacheEnabled)) {
			getTemplateCache().put(id, result);
		}

		return result;
	}

	@Override
	public void clearCacheFor(String id) {
		if (templateCache != null && templateCache.containsKey(id)) {
			templateCache.remove(id);
		}
	}

	// ------------------------------

	private IStringTemplate createGroovyShell(String template) {
		return new GroovyStringTemplate(groovyShell.parse(template));
	}

	private IStringTemplate createGroovy(String template) {
		try {
			groovy.text.Template gTemplate = simpleTemplateEngine.createTemplate(template);
			return new GroovyStringTemplate(gTemplate);
		} catch (Exception e) {
			logger.error("GroovyStringTemplate.create", e);
			throw new RuntimeException(e); //TODO
		}
	}

	private IStringTemplate createFreeMarker(String id, String template) {
		try {
			Template fmTemplate = new Template(id, template, freeMarkerCfg);
			return new FreeMarkerStringTemplate(id, fmTemplate);
		} catch (IOException e) {
			logger.error("FreeMarkerStringTemplate.create", e);
			throw new RuntimeException(e); //TODO
		}
	}

	private ICache<String, IStringTemplate> getTemplateCache() {
		if (templateCache == null) {
			templateCache = cacheService.create("DMT_STRING_TEMPLATE", 50);
		}

		return templateCache;
	}

	// ------------------------------

	private class CaseInsensitiveVariableWrapper implements ObjectWrapper {
		@Override
		public TemplateModel wrap(Object o) throws TemplateModelException {
			if (o != null) {
				if (o instanceof Map) {
					Map<String, Object> oldMap = (Map<String, Object>) o;
					Map<String, Object> newMap = new HashMap<>();
					for (Map.Entry<String, Object> entry : oldMap.entrySet()) {
						newMap.put(entry.getKey().toLowerCase(), entry.getValue());
					}

					return new SimpleHash((Map) newMap, null) {
						private static final long serialVersionUID = 4486615324575062296L;

						public TemplateModel get(String key) throws TemplateModelException {
							return super.get(key.toLowerCase());
						}
					};
				}
			}
			return null;
		}
	}
}
