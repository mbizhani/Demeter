package org.devocative.demeter.web;

import org.apache.wicket.Component;
import org.devocative.demeter.imodule.DModuleException;
import org.devocative.wickomp.IExceptionToMessageHandler;

public class DemeterExceptionToMessageHandler implements IExceptionToMessageHandler {
	private static final long serialVersionUID = 473223768980930103L;

	@Override
	public String handleMessage(Component component, Exception e) {
		if (e instanceof DModuleException) {
			DModuleException de = (DModuleException) e;
			String error = component.getString(de.getMessage(), null, de.getDefaultDescription());
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
			return component.getString(e.getMessage(), null, e.getMessage());
		} else {
			return "General Error: " + e.getClass().getSimpleName();
		}
	}
}
