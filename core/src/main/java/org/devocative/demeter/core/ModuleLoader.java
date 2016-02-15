package org.devocative.demeter.core;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.xml.XEntity;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.imodule.DModule;
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

public class ModuleLoader {
	private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);

	private static final Map<String, XModule> MODULES = new HashMap<>();
	private static ApplicationContext appCtx;

	private static Map<String, IApplicationLifecycle> APP_LIFECYCLE_BEANS;

	public static ApplicationContext getApplicationContext() {
		return appCtx;
	}

	public static Map<String, XModule> getModules() {
		return MODULES;
	}

	public static void registerSpringBean(String beanName, Object bean) {
		ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) appCtx).getBeanFactory();
		beanFactory.registerSingleton(beanName, bean);
	}

	public static void init() {
		initModules();
		initSpringContext();
		initPersistorServices();
		initDModules();

		initApplicationLifecycle();

		logger.info("### MODULE LOADER INITED");
	}

	public static void shutdown() {
		shutdownApplicationLifecycle();
		logger.info("### MODULE LOADER SHUTDOWNED");
	}

	// ---------------------------- LIFECYCLIC METHODS

	private static void initModules() {
		XStream xStream = new XStream();
		xStream.processAnnotations(XModule.class);
		xStream.alias("dependency", String.class);

		List<String> modulesName = ConfigUtil.getList(DemeterConfigKey.Modules);
		if (!modulesName.contains("Demeter")) {
			modulesName.add("Demeter");
		}

		Set<String> moduleShortNames = new HashSet<>();
		for (String moduleName : modulesName) {
			InputStream moduleXMLResource = ModuleLoader.class.getResourceAsStream(String.format("/%s.xml", moduleName));
			XModule xModule = (XModule) xStream.fromXML(moduleXMLResource);
			logger.info("Module Found: {}", moduleName);

			if (moduleShortNames.contains(xModule.getShortName())) {
				throw new DSystemException("Duplicate module short name: " + xModule.getShortName());
			}
			moduleShortNames.add(xModule.getShortName());

			MODULES.put(moduleName, xModule);
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
				IPersistorService localModulePersistor = persistors.get(String.format("%sPersistorService", module.getShortName().toLowerCase()));
				if (localModulePersistor != null) {
					if (module.getEntities() != null && module.getEntities().size() > 0) {
						String prefix = module.getShortName().toLowerCase();
						localModulePersistor.setInitData(loadEntities(module.getEntities()), prefix);
						logger.info("Local persistor for module [{}] initialized with [{}] entities.",
							moduleName, module.getEntities().size());
					} else {
						throw new DSystemException("Module has local persistor but no entities: " + moduleName);
					}
				} else {
					throw new DSystemException("No local persistor for module: " + moduleName);
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

	private static void initDModules() {
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
		APP_LIFECYCLE_BEANS = appCtx.getBeansOfType(IApplicationLifecycle.class);

		for (Map.Entry<String, IApplicationLifecycle> entry : APP_LIFECYCLE_BEANS.entrySet()) {
			entry.getValue().init();
			logger.info("Application lifecycle bean init(): {}", entry.getKey());
		}
	}

	private static void shutdownApplicationLifecycle() {
		for (Map.Entry<String, IApplicationLifecycle> entry : APP_LIFECYCLE_BEANS.entrySet()) {
			entry.getValue().shutdown();
			logger.info("Application lifecycle bean shutdown(): {}", entry.getKey());
		}
	}

	// ------------------------- PRIVATE METHODS

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
}
