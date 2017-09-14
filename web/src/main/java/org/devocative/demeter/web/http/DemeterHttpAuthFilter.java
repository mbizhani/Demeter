package org.devocative.demeter.web.http;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.wickomp.WebUtil;
import org.devocative.wickomp.http.filter.WAuthMethod;
import org.devocative.wickomp.http.filter.WBaseHttpAuthFilter;
import org.devocative.wickomp.http.filter.WHttpAuthBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DemeterHttpAuthFilter extends WBaseHttpAuthFilter {
	private static final Logger logger = LoggerFactory.getLogger(DemeterHttpAuthFilter.class);
	private static final String CORS_ORIGINS = "Access-Control-Allow-Origin";
	private static final String CORS_HEADERS = "Access-Control-Allow-Headers";
	private static final String CORS_CREDENTIAL = "Access-Control-Allow-Credentials";
	private static final String CORS_METHOD = "Access-Control-Allow-Methods ";

	private String nonce;
	private ScheduledExecutorService nonceRefreshExecutor;
	private ISecurityService securityService;
	private IUserService userService;

	@Override
	protected String calculateNonceForDigest(WHttpAuthBean authBean) {
		return nonce;
	}

	@Override
	protected String getRealm(WHttpAuthBean authBean) {
		return ConfigUtil.getString(DemeterConfigKey.SecurityRealm);
	}

	@Override
	protected String generateUserHashForDigest(WHttpAuthBean authBean) {
		return securityService.getUserDigest(authBean.getUsername());
	}

	@Override
	protected boolean authenticateByPasswordForBasic(String username, String password) {
		try {
			securityService.authenticate(username, password);
			return true;
		} catch (DemeterException e) {
			return false;
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String httpMode = ConfigUtil.getString(DemeterConfigKey.HttpAuthenticationMode);

		if ("digest".equals(httpMode)) {
			setDesiredAuthMethod(WAuthMethod.DIGEST);

			nonce = UUID.randomUUID().toString();

			nonceRefreshExecutor = Executors.newScheduledThreadPool(1);

			nonceRefreshExecutor.scheduleAtFixedRate(() -> nonce = UUID.randomUUID().toString(), 1, 1, TimeUnit.MINUTES);
		}

		securityService = DemeterCore.getApplicationContext().getBean(ISecurityService.class);
		userService = DemeterCore.getApplicationContext().getBean(IUserService.class);

		setProcessAuth(ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity));

		logger.info("DemeterHttpAuthFilter Inited: DoAuth =[{}] Method=[{}]",
			ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity), getDesiredAuthMethod());
	}

	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) {
		if (ConfigUtil.getBoolean(DemeterConfigKey.CorsEnabled) && response.getHeader(CORS_ORIGINS) == null) {
			response.addHeader(CORS_ORIGINS, ConfigUtil.getString(DemeterConfigKey.CorsHeaderOrigins));
			response.addHeader(CORS_HEADERS, ConfigUtil.getString(DemeterConfigKey.CorsHeaderHeaders));
			response.addHeader(CORS_CREDENTIAL, ConfigUtil.getString(DemeterConfigKey.CorsHeaderMethods));
			response.addHeader(CORS_METHOD, ConfigUtil.getString(DemeterConfigKey.CorsHeaderMethods));
		}

		setProcessAuth(ConfigUtil.getBoolean(DemeterConfigKey.HttpAuthFilterEnabled));
	}

	@Override
	protected void onBeforeChainAuthenticated(WHttpAuthBean authBean, HttpServletRequest request, HttpServletResponse response) {
		securityService.authenticate(userService.loadVOByUsername(authBean.getUsername()));
	}

	@Override
	protected String authenticateByOther(HttpServletRequest request, HttpServletResponse response) {
		Map<String, List<String>> params = WebUtil.toMap(request, true, true);
		UserVO userVO = securityService.authenticateByUrlParams(params);
		if (userVO != null) {
			logger.info("DemeterHttpAuthFilter: doOtherMechanism, user=[{}]", userVO);
			return userVO.getUsername();
		}
		return null;
	}

	@Override
	public void destroy() {
		if (nonceRefreshExecutor != null) {
			nonceRefreshExecutor.shutdown();
		}
	}
}
