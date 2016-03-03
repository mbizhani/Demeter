package org.devocative.demeter.web;

import org.apache.wicket.Component;
import org.devocative.demeter.imodule.DModuleException;
import org.devocative.wickomp.IExceptionToMessageHandler;

public class DemeterExceptionToMessageHandler implements IExceptionToMessageHandler {
	private static final DemeterExceptionToMessageHandler instance = new DemeterExceptionToMessageHandler();

	public static DemeterExceptionToMessageHandler get() {
		return instance;
	}

	private DemeterExceptionToMessageHandler() {
	}

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
		return DEFAULT.handleMessage(component, e);
	}
}
