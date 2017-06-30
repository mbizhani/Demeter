package org.devocative.demeter.core;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.IConfigKey;
import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.xml.XEntity;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.imodule.DModule;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.IApplicationLifecycle;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.util.*;

public class DemeterCore {
	private static final Logger logger = LoggerFactory.getLogger(DemeterCore.class);

	private static final Map<String, XModule> MODULES = new LinkedHashMap<>();

	private static final Map<ApplicationLifecyclePriority, Map<String, IApplicationLifecycle>> APP_LIFECYCLE_BEANS = new HashMap<>();

	private static ApplicationContext appCtx;
	private static boolean inited = false;
	private static boolean shuted = false;

	// ------------------------------

	public static ApplicationContext getApplicationContext() {
		return appCtx;
	}

	public static Map<String, XModule> getModules() {
		return new LinkedHashMap<>(MODULES);
	}

	public static void registerSpringBean(String beanName, Object bean) {
		ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) appCtx).getBeanFactory();
		beanFactory.registerSingleton(beanName, bean);
		beanFactory.autowireBean(bean);
	}

	public static void init() {
		init(getDefaultConfig());
	}

	public synchronized static void init(InputStream configInputStream) {
		if (!inited) {
			ConfigUtil.load(configInputStream);

			initEncDec();
			initModules();
			initSpringContext();
			initPersistorServices();
			registerDModules();

			initApplicationLifecycle();

			logger.info("### MODULE LOADER INITED");
		}

		inited = true;
	}

	public synchronized static void shutdown() {
		if (!shuted) {
			shutdownApplicationLifecycle();
			logger.info("### MODULE LOADER SHUTDOWNED");
		}

		shuted = true;
	}

	public static void generatePersistorSchemaDiff() {
		generatePersistorSchemaDiff(getDefaultConfig());
	}

	public static void generatePersistorSchemaDiff(InputStream configInputStream) {
		ConfigUtil.load(configInputStream);

		initEncDec();
		initModules();
		initSpringContext();
		initPersistorServices();

		Map<String, IPersistorService> persistorServiceMap = appCtx.getBeansOfType(IPersistorService.class);
		for (Map.Entry<String, IPersistorService> entry : persistorServiceMap.entrySet()) {
			logger.info("Persistor init: {}", entry.getKey());
			entry.getValue().init();
		}

		for (Map.Entry<String, IPersistorService> entry : persistorServiceMap.entrySet()) {
			logger.info("Persistor schema diff: {}", entry.getKey());
			entry.getValue().generateSchemaDiff();
		}
	}

	public static void applySQLSchemas(String... filters) {
		applySQLSchemas(getDefaultConfig(), filters);
	}

	public static void applySQLSchemas(InputStream configInputStream, String... filters) {
		initEncDec();
		initModules();

		DemeterCoreHelper.applySQLSchemas(MODULES.values(), filters);
	}

	public static void initEncDec() {
		boolean enableSecurity = true;
		try {
			enableSecurity = ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity);
		} catch (Exception e) {
			logger.error("", e);
		}

		StringEncryptorUtil.setBypassSecurity(!enableSecurity);

		if (ConfigUtil.getBoolean(DemeterConfigKey.SecurityKeyStoreEnabled)) {
			String tokenValue = System.getProperty(ConfigUtil.getString(DemeterConfigKey.SecurityKeyStoreToken));
			if (tokenValue == null) {
				tokenValue = System.getenv(ConfigUtil.getString(DemeterConfigKey.SecurityKeyStoreToken));
			}
			String paramValue = System.getProperty(ConfigUtil.getString(DemeterConfigKey.SecurityKeyStoreParam));
			if (paramValue == null) {
				paramValue = System.getenv(ConfigUtil.getString(DemeterConfigKey.SecurityKeyStoreParam));
			}

			if (tokenValue != null && paramValue != null) {
				String entry = ConfigUtil.getString(DemeterConfigKey.SecurityKeyStoreEntry);
				try {
					StringEncryptorUtil.init(DemeterCore.class.getResourceAsStream("/demeter.ks"), tokenValue, entry, paramValue);
					logger.info("StringEncryptorUtil INITED");
				} catch (Exception e) {
					logger.error("StringEncryptorUtil Init Error: " + e);
				}
			} else {
				logger.error("StringEncryptorUtil Init Problem: KeyStoreToken=[{}] KeyStoreParam=[{}]",
					tokenValue != null, paramValue != null);
			}
		}
	}

	// ------------------------------

	private static void initModules() {
		XStream xStream = new XStream();
		xStream.processAnnotations(XModule.class);
		xStream.alias("dependency", String.class);

		List<String> modulesName = ConfigUtil.getList(DemeterConfigKey.Modules);
		if (!modulesName.contains("Demeter")) {
			modulesName.add(0, "Demeter");
		}

		Set<String> moduleShortNames = new HashSet<>();
		for (String moduleName : modulesName) {
			InputStream moduleXMLResource = DemeterCore.class.getResourceAsStream(String.format("/%s.xml", moduleName));
			XModule xModule = (XModule) xStream.fromXML(moduleXMLResource);
			logger.info("Module Found: {}", moduleName);

			if (moduleShortNames.contains(xModule.getShortName())) {
				throw new DSystemException("Duplicate module short name: " + xModule.getShortName());
			}
			moduleShortNames.add(xModule.getShortName());

			MODULES.put(moduleName, xModule);

			loadConfigKeys(xModule);
		}
	}

	private static void initSpringContext() {
		boolean clientMode = ConfigUtil.getString(DemeterConfigKey.ServiceRemoteHost) != null;
		logger.info("Client Mode: {}", clientMode);

		String[] springConfigLocations = new String[MODULES.size()];
		String springPrefixConfig = clientMode ? "client" : "local";
		int i = 0;
		for (Map.Entry<String, XModule> moduleEntry : MODULES.entrySet()) {
			String springXML = String.format("/%s%s.xml", springPrefixConfig, moduleEntry.getValue().getShortName());
			logger.info("Loading Spring Config Location: {}", springXML);
			springConfigLocations[i] = springXML;
			i++;
		}

		appCtx = new ClassPathXmlApplicationContext(springConfigLocations);

		for (String bean : appCtx.getBeanDefinitionNames()) {
			logger.info("\tSPRING BEAN: {}", bean);
		}
	}

	private static void initPersistorServices() {
		Map<String, IPersistorService> persistors = appCtx.getBeansOfType(IPersistorService.class);

		List<Class> dmtPersistorServiceEntities = new ArrayList<>();
		for (Map.Entry<String, XModule> moduleEntry : MODULES.entrySet()) {
			String moduleName = moduleEntry.getKey();
			XModule module = moduleEntry.getValue();
			if (module.isLocalPersistorService()) {
				String prefix = module.getShortName().toLowerCase();
				IPersistorService localModulePersistor = persistors.get(String.format("%sPersistorService", prefix));
				if (localModulePersistor != null) {
					if (module.getEntities() != null && module.getEntities().size() > 0) {
						localModulePersistor.setInitData(loadEntities(module.getEntities()), prefix);
						logger.info("Local persistor for module [{}] initialized with [{}] entities.",
							moduleName, module.getEntities().size());
					} else {
						throw new DSystemException("Module has local persistor but no entities: " + moduleName);
					}
				} else {
					throw new DSystemException("No local persistor bean for module: " + moduleName);
				}
			} else {
				if (module.getEntities() != null && module.getEntities().size() > 0) {
					dmtPersistorServiceEntities.addAll(loadEntities(module.getEntities()));
					logger.info("Module has {} entities.", module.getEntities().size());
				}
			}
		}

		persistors.get("dmtPersistorService").setInitData(dmtPersistorServiceEntities, "dmt");
		logger.info("Demeter persistor initialized with [{}] entities.", dmtPersistorServiceEntities.size());
	}

	private static void registerDModules() {
		for (XModule module : MODULES.values()) {
			try {
				String beanName = String.format("%sDModule", module.getShortName().toLowerCase());
				Class<?> moduleMainClass = Class.forName(module.getMainClass());
				DModule dModule = (DModule) moduleMainClass.newInstance();
				registerSpringBean(beanName, dModule);
				logger.info("DModule bean created: {}", beanName);
			} catch (Exception e) {
				throw new DSystemException("DModule class: " + module.getMainClass(), e);
			}
		}
	}

	private static void initApplicationLifecycle() {
		Map<String, IApplicationLifecycle> beansOfType = appCtx.getBeansOfType(IApplicationLifecycle.class);
		for (Map.Entry<String, IApplicationLifecycle> entry : beansOfType.entrySet()) {
			if (entry.getValue().getLifecyclePriority() == null) {
				throw new DSystemException("IApplicationLifecycle has no priority: " + entry.getKey());
			}

			if (!APP_LIFECYCLE_BEANS.containsKey(entry.getValue().getLifecyclePriority())) {
				APP_LIFECYCLE_BEANS.put(entry.getValue().getLifecyclePriority(), new HashMap<String, IApplicationLifecycle>());
			}

			APP_LIFECYCLE_BEANS
				.get(entry.getValue().getLifecyclePriority())
				.put(entry.getKey(), entry.getValue());
		}

		for (ApplicationLifecyclePriority priority : ApplicationLifecyclePriority.values()) {
			Map<String, IApplicationLifecycle> lifecycleMap = APP_LIFECYCLE_BEANS.get(priority);
			for (Map.Entry<String, IApplicationLifecycle> entry : lifecycleMap.entrySet()) {
				entry.getValue().init();
				logger.info("Application lifecycle bean ({}) init(): {}", priority, entry.getKey());
			}
		}

		Map<String, IPersistorService> iPersistorServiceMap = appCtx.getBeansOfType(IPersistorService.class);
		for (IPersistorService persistorService : iPersistorServiceMap.values()) {
			persistorService.endSession();
		}
	}

	private static void shutdownApplicationLifecycle() {
		List<ApplicationLifecyclePriority> priorities = Arrays.asList(ApplicationLifecyclePriority.values());
		Collections.reverse(priorities);

		for (ApplicationLifecyclePriority priority : ApplicationLifecyclePriority.values()) {
			Map<String, IApplicationLifecycle> lifecycleMap = APP_LIFECYCLE_BEANS.get(priority);
			for (Map.Entry<String, IApplicationLifecycle> entry : lifecycleMap.entrySet()) {
				entry.getValue().shutdown();
				logger.info("Application lifecycle bean ({}) shutdown(): {}", priority, entry.getKey());
			}
		}
	}

	// ---------------

	private static List<Class> loadEntities(List<XEntity> entities) {
		List<Class> classes = new ArrayList<>();
		for (XEntity entity : entities) {
			try {
				classes.add(Class.forName(entity.getType()));
			} catch (ClassNotFoundException e) {
				throw new DSystemException("Entity class not found: " + entity);
			}
		}
		return classes;
	}

	private static void loadConfigKeys(XModule xModule) {
		try {
			if (xModule.getConfigKeyClass() != null) {
				Class<?> enumClass = Class.forName(xModule.getConfigKeyClass());
				if (enumClass.isEnum()) {
					Object[] enumConstants = enumClass.getEnumConstants();
					for (Object enumConstant : enumConstants) {
						ConfigUtil.add((IConfigKey) enumConstant);
					}
				} else {
					throw new DSystemException("ConfigKey class must be enum for module: " + xModule.getShortName());
				}
			} else {
				throw new DSystemException("ConfigKey class not found for module: " + xModule.getShortName());
			}
		} catch (Exception e) {
			logger.error(String.format("Loading module [%s] config keys", xModule.getShortName()), e);
		}
	}

	private static InputStream getDefaultConfig() {
		return DemeterCore.class.getResourceAsStream("/config.properties");
	}
}
