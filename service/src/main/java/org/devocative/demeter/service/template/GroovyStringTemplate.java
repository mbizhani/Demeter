package org.devocative.demeter.service.template;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.text.Template;
import org.devocative.demeter.iservice.template.BaseStringTemplate;

import java.util.Map;

public class GroovyStringTemplate extends BaseStringTemplate {
	private Template template;
	private Script script;

	public GroovyStringTemplate(Template template) {
		this.template = template;
	}

	public GroovyStringTemplate(Script script) {
		this.script = script;
	}

	@Override
	public String process(Map<String, Object> params) {
		if (template != null) {
			Writable writable = template.make(params);
			return writable != null ? writable.toString() : null;
		} else {
			Binding binding = new Binding();
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				binding.setVariable(entry.getKey(), entry.getValue());
			}

			script.setBinding(binding);
			Object run = script.run();
			return run != null ? run.toString() : null;
		}
	}

	@Override
	public Object unwrap() {
		return template != null ? template : script;
	}
}
