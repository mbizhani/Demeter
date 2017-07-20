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
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.wickomp.WDefaults;
import org.devocative.wickomp.async.AsyncMediator;
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

	// ------------------------------

	public static DemeterWebApplication get() {
		return (DemeterWebApplication) WebApplication.get();
	}

	// ------------------------------ WebApplication OVERRIDES

	@Override
	public Class<? extends Page> getHomePage() {
		return Index.class;
	}

	@Override
	protected void init() {
		logger.info("*************************");
		logger.info("** Demeter Application **");
		logger.info("** Context Path: {}", getServletContext().getContextPath());

		getComponentInstantiationListeners().add(new SpringComponentInjector(this, DemeterCore.getApplicationContext()));

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		getRequestCycleListeners().add(new DemeterRequestCycleListener());
		getRequestCycleSettings().setTimeout(Duration.minutes(ConfigUtil.getInteger(DemeterConfigKey.WebRequestTimeout)));
		getResourceSettings().setThrowExceptionOnMissingResource(!ConfigUtil.getBoolean(DemeterConfigKey.WebIgnoreMissedResource));

		WDefaults.setExceptionToMessageHandler(new DemeterExceptionToMessageHandler());

		mountPage(APP_INNER_CTX, Index.class);

		mountResource(String.format("%s/dmt/getfile/${fileid}", APP_INNER_CTX), new FileStoreResourceReference(DemeterCore.getApplicationContext()));

		initModulesForWeb();

		AsyncMediator.init(this);

		logger.info("** Demeter Application Up! **");
		logger.info("*****************************");
	}

	@Override
	public WebSession newSession(Request request, Response response) {
		return new DemeterWebSession(request);
	}

	@Override
	protected void onDestroy() {
		AsyncMediator.shutdown();
	}

	// ------------------------------

	public String getInnerContext() {
		return APP_INNER_CTX;
	}

	public String getContextPath() {
		return getServletContext().getContextPath();
	}

	public List<String> getModulesRelatedCSS() {
		return modulesRelatedCSS;
	}

	// ------------------------------

	private void initModulesForWeb() {
		String appBaseDir = getServletContext().getRealPath(".");

		Map<String, XModule> modules = DemeterCore.getModules();
		for (Map.Entry<String, XModule> moduleEntry : modules.entrySet()) {
			XModule xModule = moduleEntry.getValue();
			getResourceSettings().getStringResourceLoaders().add(0, new BundleStringResourceLoader(xModule.getMainResource()));

			loadWebDModule(xModule.getMainResource(), xModule.getShortName().toLowerCase());

			// TODO theme-based CSS finding & loading
			String moduleRelatedCSS = String.format("/styles/main/d_%s.css", xModule.getShortName().toLowerCase());
			if (new File(appBaseDir + moduleRelatedCSS).exists()) {
				modulesRelatedCSS.add(moduleRelatedCSS);
				logger.info("Module related CSS: {}", moduleRelatedCSS);
			}

			logger.info("Module [{}] inited for web", moduleEntry.getKey());
		}
	}

	private void loadWebDModule(String classFQN, String module) {
		WebDModule webDModule = null;
		try {
			Class<? extends WebDModule> webDModuleClass = (Class<? extends WebDModule>) Class.forName(classFQN);
			webDModule = webDModuleClass.newInstance();
		} catch (Exception e) {
			logger.info("No WebDModule for module: {}", module);
		}

		if (webDModule != null) {
			DemeterCore.registerSpringBean(module + "WebDModule", webDModule);
			webDModule.init();
			logger.info("WebDModule created and inited successfully: {}", module);
		}
	}
}