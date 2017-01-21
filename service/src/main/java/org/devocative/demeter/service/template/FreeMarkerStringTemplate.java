package org.devocative.demeter.service.template;

import freemarker.template.Template;
import org.devocative.demeter.iservice.template.BaseStringTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Map;

public class FreeMarkerStringTemplate extends BaseStringTemplate {
	private static final Logger logger = LoggerFactory.getLogger(FreeMarkerStringTemplate.class);

	private String id;
	private Template template;

	// ------------------------------

	public FreeMarkerStringTemplate(String id, Template template) {
		this.id = id;
		this.template = template;
	}

	@Override
	public Object process(Map<String, Object> params) {
		Object dataModel = params;
		if (convertValuesToString) {
			dataModel = convertValuesToString(params);
		}

		StringWriter out = new StringWriter();
		try {
			logger.debug("FreeMarkerStringTemplate.process: id = {}", id);

			template.process(dataModel, out);
			return out.toString();
		} catch (Exception e) {
			logger.error("FreeMarkerStringTemplate.process", e);
			throw new RuntimeException(e); //TODO
		}
	}

	@Override
	public Object unwrap() {
		return template;
	}
}
