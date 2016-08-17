package org.devocative.demeter.web.http;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.wickomp.http.filter.WAuthMethod;
import org.devocative.wickomp.http.filter.WBaseHttpAuthFilter;
import org.devocative.wickomp.http.filter.WHttpAuthBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DemeterHttpAuthFilter extends WBaseHttpAuthFilter {
	private static final Logger logger = LoggerFactory.getLogger(DemeterHttpAuthFilter.class);

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
		}

		if (getDesiredAuthMethod() == WAuthMethod.DIGEST) {
			nonce = UUID.randomUUID().toString();

			nonceRefreshExecutor = Executors.newScheduledThreadPool(1);

			nonceRefreshExecutor.scheduleAtFixedRate(new Runnable() {

				public void run() {
					nonce = UUID.randomUUID().toString();
				}
			}, 1, 1, TimeUnit.MINUTES);
		}

		securityService = ModuleLoader.getApplicationContext().getBean(ISecurityService.class);
		userService = ModuleLoader.getApplicationContext().getBean(IUserService.class);

		setProcessAuth(ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity));

		logger.info("DemeterHttpAuthFilter Inited: DoAuth =[{}] Method=[{}]",
			ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity), getDesiredAuthMethod());
	}

	@Override
	protected void onBeforeChainAuthenticated(WHttpAuthBean authBean) {
		securityService.authenticate(userService.loadVOByUsername(authBean.getUsername()));
	}

	@Override
	public void destroy() {
		if (nonceRefreshExecutor != null) {
			nonceRefreshExecutor.shutdown();
		}
	}
}
