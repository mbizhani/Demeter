package org.devocative.demeter.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.time.Duration;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.core.xml.XModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DemeterWebApplication extends WebApplication {
	private static final Logger logger = LoggerFactory.getLogger(DemeterWebApplication.class);

	private static final String APP_INNER_CTX = "/dvc";

	private List<String> modulesRelatedCSS = new ArrayList<>();

	public static DemeterWebApplication get() {
		return (DemeterWebApplication) WebApplication.get();
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return Index.class;
	}

	@Override
	protected void init() {
		logger.info("*************************");
		logger.info("** Demeter Application **");
		logger.info("** Context Path: {}", getServletContext().getContextPath());

		ModuleLoader.init();
		getComponentInstantiationListeners().add(new SpringComponentInjector(this, ModuleLoader.getApplicationContext()));

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		getRequestCycleListeners().add(new DemeterRequestCycleListener());
		getRequestCycleSettings().setTimeout(Duration.minutes(ConfigUtil.getInteger(DemeterConfigKey.WebRequestTimeout)));
		getResourceSettings().setThrowExceptionOnMissingResource(!ConfigUtil.getBoolean(DemeterConfigKey.WebIgnoreMissedResource));

		mountPage(APP_INNER_CTX, Index.class);

		initModulesForWeb();

		logger.info("** Demeter Application Up! **");
		logger.info("*****************************");
	}

	public String getInnerContext() {
		return APP_INNER_CTX;
	}

	public List<String> getModulesRelatedCSS() {
		return modulesRelatedCSS;
	}

	@Override
	public WebSession newSession(Request request, Response response) {
		return new DemeterWebSession(request);
	}

	@Override
	protected void onDestroy() {
		ModuleLoader.shutdown();
	}

	private void initModulesForWeb() {
		String appBaseDir = getServletContext().getRealPath(".");

		Map<String, XModule> modules = ModuleLoader.getModules();
		for (Map.Entry<String, XModule> moduleEntry : modules.entrySet()) {
			XModule xModule = moduleEntry.getValue();
			getResourceSettings().getStringResourceLoaders().add(0, new BundleStringResourceLoader(xModule.getMainResource()));

			// TODO theme-based CSS finding & loading
			String moduleRelatedCSS = String.format("/styles/main/d_%s.css", xModule.getShortName().toLowerCase());
			if (new File(appBaseDir + moduleRelatedCSS).exists()) {
				modulesRelatedCSS.add(moduleRelatedCSS);
				logger.info("Module related CSS: {}", moduleRelatedCSS);
			}

			logger.info("Module [{}] inited for web", moduleEntry.getKey());
		}
	}
}