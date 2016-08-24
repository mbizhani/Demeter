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

import java.util.List;
import java.util.Locale;

public class DemeterWebSession extends WebSession implements OUserPreference {
	private static final long serialVersionUID = 4582797765048584757L;

	private static final Logger logger = LoggerFactory.getLogger(DemeterWebSession.class);

	private UserVO userVO;
	private Class<? extends DPage> originalDPage;
	private List<String> originalParams;

	// ------------------------------ CONSTRUCTORS

	public DemeterWebSession(Request request) {
		super(request);

		setLocale(new Locale("fa", "IR"));
	}

	// ------------------------------ ACCESSORS

	public UserVO getUserVO() {
		return userVO;
	}

	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

	public Class<? extends DPage> getOriginalDPage() {
		return originalDPage;
	}

	public DemeterWebSession setOriginalDPage(Class<? extends DPage> originalDPage) {
		this.originalDPage = originalDPage;
		return this;
	}

	public List<String> getOriginalParams() {
		return originalParams;
	}

	public DemeterWebSession setOriginalParams(List<String> originalParams) {
		this.originalParams = originalParams;
		return this;
	}

	// ------------------------------ OUserPreference

	//TODO add following methods to UserVO and return from userVO reference
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

	// ------------------------------

	public void removeOriginal() {
		setOriginalDPage(null);
		setOriginalParams(null);
	}

	public static DemeterWebSession get() {
		return (DemeterWebSession) WebSession.get();
	}
}
