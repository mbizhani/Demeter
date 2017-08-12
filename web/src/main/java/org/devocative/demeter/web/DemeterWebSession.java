package org.devocative.demeter.web;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.ECalendar;
import org.devocative.demeter.entity.ELayoutDirection;
import org.devocative.demeter.vo.UserVO;
import org.devocative.wickomp.opt.OCalendar;
import org.devocative.wickomp.opt.OLayoutDirection;
import org.devocative.wickomp.opt.OUserPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DemeterWebSession extends WebSession implements OUserPreference {
	private static final long serialVersionUID = 4582797765048584757L;

	private static final Logger logger = LoggerFactory.getLogger(DemeterWebSession.class);

	private UserVO userVO;
	private Class<? extends DPage> originalDPage;
	private List<String> originalParams;
	private IRequestParameters queryParameters;

	// ------------------------------

	public DemeterWebSession(Request request) {
		super(request);
	}

	// ------------------------------

	public UserVO getUserVO() {
		return userVO;
	}

	public void setUserVO(UserVO userVO) {
		if (userVO == null) {
			throw new DemeterException(DemeterErrorCode.InvalidUser);
		}
		this.userVO = userVO;
		setLocale(new Locale(userVO.getLocale().getCode()));
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

	public IRequestParameters getQueryParameters() {
		return queryParameters;
	}

	public DemeterWebSession setQueryParameters(IRequestParameters queryParameters) {
		this.queryParameters = queryParameters;
		return this;
	}

	// ---------------

	@Override
	public OCalendar getCalendar() {
		OCalendar result = OCalendar.Gregorian;
		if (ECalendar.JALALI.equals(userVO.getCalendar())) {
			result = OCalendar.Persian;
		}
		return result;
	}

	@Override
	public TimeZone getTimeZone() {
		throw new RuntimeException("Not Implemented!");
	}

	@Override
	public String getDatePattern() {
		return userVO.getDatePatternType().toString();
	}

	@Override
	public String getDateTimePattern() {
		return userVO.getDateTimePatternType().toString();
	}

	@Override
	public OLayoutDirection getLayoutDirection() {
		OLayoutDirection result = OLayoutDirection.LTR;
		if (ELayoutDirection.RTL.equals(userVO.getLayoutDirection())) {
			result = OLayoutDirection.RTL;
		}
		return result;
	}

	@Override
	public void onInvalidate() {
		logger.info("Session invalidated: user={}", userVO.getUsername());
	}

	// ---------------

	public void removeOriginal() {
		setOriginalDPage(null);
		setOriginalParams(null);
		setQueryParameters(null);
	}

	// ---------------

	public static DemeterWebSession get() {
		return (DemeterWebSession) WebSession.get();
	}
}
