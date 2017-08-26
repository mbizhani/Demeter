package org.devocative.demeter.service.template;

import groovy.lang.Binding;
import groovy.lang.Script;
import org.devocative.demeter.iservice.template.BaseStringTemplate;

import java.util.Map;

public class GroovyScript extends BaseStringTemplate<Script> {
	private Script script;

	public GroovyScript(Script script) {
		this.script = script;
	}

	@Override
	public Object process(Map<String, Object> params) {
		Binding binding = new Binding();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			binding.setVariable(entry.getKey(), entry.getValue());
		}
		script.setBinding(binding);
		return script.run();
	}

	@Override
	public Script unwrap() {
		return script;
	}
}
