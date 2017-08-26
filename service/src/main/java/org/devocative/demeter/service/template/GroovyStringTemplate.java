package org.devocative.demeter.service.template;

import groovy.lang.Writable;
import groovy.text.Template;
import org.devocative.demeter.iservice.template.BaseStringTemplate;

import java.util.Map;

public class GroovyStringTemplate extends BaseStringTemplate<Template> {
	private Template template;

	public GroovyStringTemplate(Template template) {
		this.template = template;
	}

	@Override
	public Object process(Map<String, Object> params) {
		Writable writable = template.make(params);
		return writable != null ? writable.toString() : null;
	}

	@Override
	public Template unwrap() {
		return template;
	}
}
