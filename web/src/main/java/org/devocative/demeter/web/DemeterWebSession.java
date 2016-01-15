package org.devocative.demeter.web;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.devocative.wickomp.opt.OCalendar;
import org.devocative.wickomp.opt.OLayoutDirection;
import org.devocative.wickomp.opt.OUserPreference;

import java.util.Locale;

public class DemeterWebSession extends WebSession implements OUserPreference {

	public static DemeterWebSession get() {
		return (DemeterWebSession) WebSession.get();
	}

	public DemeterWebSession(Request request) {
		super(request);

		setLocale(new Locale("fa", "IR"));
	}

	@Override
	public OCalendar getCalendar() {
		return OCalendar.Persian;
	}

	@Override
	public String getDatePattern() {
		return "yyyy/MM/dd";
	}

	@Override
	public String getDateTimePattern() {
		return "yyyy/MM/dd HH:mm:ss";
	}

	@Override
	public OLayoutDirection getLayoutDirection() {
		return OLayoutDirection.RTL;
	}
}
