package org.devocative.demeter.web;

import org.apache.wicket.Component;
import org.devocative.demeter.imodule.DModuleException;
import org.devocative.wickomp.IExceptionToMessageHandler;

public class DemeterExceptionToMessageHandler implements IExceptionToMessageHandler {
	@Override
	public String handleMessage(Component component, Exception e) {
		if (e instanceof DModuleException) {
			DModuleException de = (DModuleException) e;
			String error = component.getString(de.getMessage(), null, de.getDefaultDescription());
			if (de.getErrorParameter() != null) {
				error += ": " + de.getErrorParameter();
			}
			return error;
		}
		return component.getString(e.getMessage(), null, e.getMessage());
	}
}
