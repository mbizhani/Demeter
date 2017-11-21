package org.devocative.demeter.service.template;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;
import org.devocative.demeter.iservice.template.BaseStringTemplate;

import java.util.Map;

public class GroovyDelegatingScript extends BaseStringTemplate<DelegatingScript> {
	private DelegatingScript delegatingScript;

	public GroovyDelegatingScript(DelegatingScript delegatingScript) {
		this.delegatingScript = delegatingScript;
	}

	@Override
	public Object process(Map<String, Object> params) {
		if (params.containsKey(GROOVY_DELEGATE_KEY)) {
			Object delegate = params.get(GROOVY_DELEGATE_KEY);
			params.remove(GROOVY_DELEGATE_KEY);
			delegatingScript.setDelegate(delegate);
		} else {
			throw new RuntimeException("No delegate object in params map, key=" + GROOVY_DELEGATE_KEY);
		}

		Binding binding = new Binding();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			binding.setVariable(entry.getKey(), entry.getValue());
		}
		delegatingScript.setBinding(binding);
		return delegatingScript.run();
	}

	@Override
	public DelegatingScript unwrap() {
		return delegatingScript;
	}
}
