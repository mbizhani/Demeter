package org.devocative.demeter.web;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.devocative.demeter.vo.UserVO;
import org.devocative.wickomp.async.AsyncMediator;
import org.devocative.wickomp.opt.OCalendar;
import org.devocative.wickomp.opt.OLayoutDirection;
import org.devocative.wickomp.opt.OUserPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class DemeterWebSession extends WebSession implements OUserPreference {
	private static Logger logger = LoggerFactory.getLogger(DemeterWebSession.class);

	private UserVO userVO;

	public DemeterWebSession(Request request) {
		super(request);

		setLocale(new Locale("fa", "IR"));
	}

	public UserVO getUserVO() {
		return userVO;
	}

	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
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

	@Override
	public void onInvalidate() {
		logger.info("Session invalidated: user={}", userVO.getUsername());

		AsyncMediator.handleSessionExpiration(userVO.getUsername(), getId());
	}

	public static DemeterWebSession get() {
		return (DemeterWebSession) WebSession.get();
	}
}
