package org.devocative.demeter.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.time.Duration;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.core.xml.XDPage;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.iservice.IPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DemeterWebApplication extends WebApplication {
	private static final Logger logger = LoggerFactory.getLogger(DemeterWebApplication.class);

	@Override
	public Class<? extends Page> getHomePage() {
		return Index.class;
	}

	@Override
	protected void init() {
		logger.info("*************************");
		logger.info("** Demeter Application **");
		logger.info("** Context Path: {}", getServletContext().getContextPath());

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		//getRequestCycleListeners().add(new WPortalRequestCycleListener(portalService, isClientMode));
		getRequestCycleSettings().setTimeout(Duration.minutes(ConfigUtil.getInteger("dmt.web.request.timeout", 10)));

		//getResourceSettings().getStringResourceLoaders().add(0, new ClassStringResourceLoader(portalModule.getClass()));
		getResourceSettings().setThrowExceptionOnMissingResource(!ConfigUtil.getBoolean("dmt.web.ignore.missed.resource", false));

		mountPage("/", Index.class);
		ModuleLoader.init();
		getComponentInstantiationListeners().add(new SpringComponentInjector(this, ModuleLoader.getApplicationContext()));

		initDPages();
	}

	private void initDPages() {
		IPageService pageService = ModuleLoader.getApplicationContext().getBean(IPageService.class);
		pageService.disableAllPageInfo();

		Map<String, XModule> modules = ModuleLoader.getModules();
		for (Map.Entry<String, XModule> moduleEntry : modules.entrySet()) {
			XModule module = moduleEntry.getValue();
			List<XDPage> dPages = module.getDPages();
			for (XDPage dPage : dPages) {
				pageService.addOrUpdatePageInfo(dPage.getType(), module.getShortName().toLowerCase(),
					dPage.getUri(), dPage.getTitle());
			}
		}
	}

	@Override
	public WebSession newSession(Request request, Response response) {
		return new DemeterWebSession(request);
	}

	@Override
	protected void onDestroy() {
		ModuleLoader.shutdown();
	}
}