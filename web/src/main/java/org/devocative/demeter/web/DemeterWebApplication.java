package org.devocative.demeter.web;

import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.protocol.http.CsrfPreventionRequestCycleListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.https.HttpsConfig;
import org.apache.wicket.protocol.https.HttpsMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.time.Duration;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.IDemeterCoreEventListener;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.wickomp.WDefaults;
import org.devocative.wickomp.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DemeterWebApplication extends WebApplication implements IDemeterCoreEventListener {
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
	public void afterUpSuccessfully() {
		getRequestCycleListeners().add(new DemeterRequestCycleListener());

		mountPage(APP_INNER_CTX, Index.class);

		mountResource(String.format("%s/dmt/getfile/${fileid}", APP_INNER_CTX), new FileStoreResourceReference(DemeterCore.get().getApplicationContext()));

		initModulesForWeb();

		if (ConfigUtil.getBoolean(DemeterConfigKey.UrlCrypticEnabled)) {
			setRootRequestMapper(new CryptoMapper(getRootRequestMapper(), this));
			logger.info("URL Cryptic Enabled");
		}

		if (ConfigUtil.getBoolean(DemeterConfigKey.HttpsEnabled)) {
			int httpPort = ConfigUtil.getInteger(DemeterConfigKey.HttpPort);
			int httpsPort = ConfigUtil.getInteger(DemeterConfigKey.HttpsPort);
			setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new HttpsConfig(httpPort, httpsPort)));
			logger.info("HTTPS Enabled: HTTP Port=[{}], HTTPS Port=[{}]", httpPort, httpsPort);
		}

		if (ConfigUtil.getBoolean(DemeterConfigKey.CsrfPreventionEnabled)) {
			getRequestCycleListeners().add(new CsrfPreventionRequestCycleListener());
			logger.info("Csrf Prevention Enabled");
		}

		logger.info("** Demeter Application Up! **");
		logger.info("*****************************");
	}

	@Override
	public WebSession newSession(Request request, Response response) {
		return new DemeterWebSession(request);
	}

	// ------------------------------

	@Override
	protected void init() {
		logger.info("*************************");
		logger.info("** Demeter Application **");
		logger.info("** Context Path: {}", getServletContext().getContextPath());

		getComponentInstantiationListeners().add(new SpringComponentInjector(this, DemeterCore.get().getApplicationContext()));

		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

		getRequestCycleSettings().setTimeout(Duration.minutes(ConfigUtil.getInteger(DemeterConfigKey.WebRequestTimeout)));
		getResourceSettings().setThrowExceptionOnMissingResource(!ConfigUtil.getBoolean(DemeterConfigKey.WebIgnoreMissedResource));

		WDefaults.setExceptionToMessageHandler(new DemeterExceptionToMessageHandler());

		if (DemeterCore.get().isStartedSuccessfully()) {
			afterUpSuccessfully();
		} else {
			mountPage(APP_INNER_CTX, StartupHandlerPage.class);
			DemeterCore.get().addCoreEvent(this);
		}
	}

	@Override
	protected IConverterLocator newConverterLocator() {
		ConverterLocator converterLocator = new ConverterLocator();

		String jsonConfig = ConfigUtil.getString(DemeterConfigKey.WebReplaceCharForString);
		if (jsonConfig != null && !jsonConfig.trim().isEmpty()) {
			Map map = WebUtil.fromJson(jsonConfig, Map.class);
			logger.info("Demeter Application: Char Replacement {}", map);

			if (!map.isEmpty()) {
				converterLocator.set(String.class, new IConverter<Object>() {
					private static final long serialVersionUID = 8964294171953142318L;

					@Override
					public Object convertToObject(String s, Locale locale) throws ConversionException {
						try {
							if (map.containsKey(locale.getLanguage())) {
								List<Map> replacements = (List<Map>) map.get(locale.getLanguage());
								for (Map replacement : replacements) {
									String from = (String) replacement.get("from");
									String to = (String) replacement.get("to");
									s = s.replaceAll(from, to);
								}
								return s;
							}
						} catch (Exception e) {
							logger.error("DemeterApplication.newConverterLocator", e);
						}

						return s;
					}

					@Override
					public String convertToString(Object o, Locale locale) {
						return o != null ? o.toString() : null;
					}
				});
			}
		}

		return converterLocator;
	}

	@Override
	protected void onDestroy() {
		logger.info("DemeterWebApplication.onDestroy()");
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

		Map<String, XModule> modules = DemeterCore.get().getModules();
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
			DemeterCore.get().registerSpringBean(module + "WebDModule", webDModule);
			webDModule.init();
			logger.info("WebDModule created and inited successfully: {}", module);
		}
	}
}