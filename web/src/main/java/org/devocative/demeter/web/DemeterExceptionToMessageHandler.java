package org.devocative.demeter.web;

import org.apache.wicket.Component;
import org.devocative.demeter.imodule.DModuleException;
import org.devocative.wickomp.IExceptionToMessageHandler;
import org.devocative.wickomp.WebUtil;

public class DemeterExceptionToMessageHandler implements IExceptionToMessageHandler {
	private static final long serialVersionUID = 473223768980930103L;

	@Override
	public String handleMessage(Component component, Throwable e) {
		if (e instanceof DModuleException) {
			DModuleException de = (DModuleException) e;
			String error = getMessage(component, de.getMessage(), de.getDefaultDescription());

			if (de.getErrorParameter() != null) {
				error += ": " + de.getErrorParameter();
			} else if (de.getCause() != null) {
				if (de.getCause().getMessage() != null) {
					error += ": " + de.getCause().getMessage();
				} else {
					error += ": " + de.getCause().getClass().getName();
				}
			}
			return error;
		}

		if (e.getMessage() != null) {
			return getMessage(component, e.getMessage(), e.getMessage());
		} else {
			return String.format("[Error(%s)]", e.getClass().getName());
		}
	}

	private String getMessage(Component cmp, String key, String defaultValue) {
		if (cmp == null) {
			return WebUtil.getStringOfResource(key, defaultValue);
		} else {
			return cmp.getString(key, null, defaultValue);
		}
	}
}
