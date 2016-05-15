package org.devocative.demeter.web;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.wickomp.http.filter.WBaseHttpDigestAuthFilter;
import org.devocative.wickomp.http.filter.WHttpAuthBean;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DemeterDigestAuthFilter extends WBaseHttpDigestAuthFilter {
	private String nonce;
	private ScheduledExecutorService nonceRefreshExecutor;
	private ISecurityService securityService;

	@Override
	protected String calculateNonce(WHttpAuthBean authBean) {
		return nonce;
	}

	@Override
	protected String getRealm(WHttpAuthBean authBean) {
		return ConfigUtil.getString(DemeterConfigKey.SecurityRealm);
	}

	@Override
	protected String generateUserHash(WHttpAuthBean authBean) {
		return securityService.getUserDigest(authBean.getUsername());
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		nonce = UUID.randomUUID().toString();

		nonceRefreshExecutor = Executors.newScheduledThreadPool(1);

		nonceRefreshExecutor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				nonce = UUID.randomUUID().toString();
			}
		}, 1, 1, TimeUnit.MINUTES);

		securityService = ModuleLoader.getApplicationContext().getBean(ISecurityService.class);
	}

	@Override
	public void destroy() {
		nonceRefreshExecutor.shutdown();
	}
}
