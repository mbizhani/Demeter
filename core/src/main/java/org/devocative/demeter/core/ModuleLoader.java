package org.devocative.demeter.core;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.core.xml.Entity;
import org.devocative.demeter.core.xml.Module;
import org.devocative.demeter.iservice.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.util.*;

public class ModuleLoader {
	private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);

	private static final Map<String, Module> MODULES = new HashMap<>();
	private static ApplicationContext appCtx;

	public static ApplicationContext getAppCtx() {
		return appCtx;
	}


	public static void start() {
		init();
		initSpringContext();
		initPersistorServices();
	}

	public static void stop() {
		shutdownPersistorServices();
	}

	private static void init() {
		XStream xStream = new XStream();
		xStream.processAnnotations(Module.class);
		xStream.alias("dependency", String.class);

		List<String> modulesName = ConfigUtil.getList("dmt.modules", Collections.singletonList("Demeter"));
		for (String moduleName : modulesName) {
			InputStream moduleXMLResource = ModuleLoader.class.getResourceAsStream(String.format("/%s.xml", moduleName));
			Module module = (Module) xStream.fromXML(moduleXMLResource);
			logger.info("Module Found: {}", moduleName);
			MODULES.put(moduleName, module);
		}
	}

	private static void initSpringContext() {
		boolean clientMode = ConfigUtil.getString("dmt.service.remote.host", null) != null;
		logger.info("Client Mode: {}", clientMode);

		String[] springConfigLocations = new String[MODULES.size()];
		String springPrefixConfig = clientMode ? "client" : "local";
		int i = 0;
		for (Map.Entry<String, Module> moduleEntry : MODULES.entrySet()) {
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
		for (Map.Entry<String, Module> moduleEntry : MODULES.entrySet()) {
			String moduleName = moduleEntry.getKey();
			Module module = moduleEntry.getValue();
			if (module.isLocalPersistorService()) {
				IPersistorService localModulePersistor = persistors.get(String.format("%sPersistorService", module.getShortName().toLowerCase()));
				if (localModulePersistor != null) {
					if (module.getEntities() != null && module.getEntities().size() > 0) {
						String prefix = module.getShortName().toLowerCase();
						localModulePersistor.init(
							loadEntities(module.getEntities()),
							ConfigUtil.getString(true, String.format("%s.db.driver", prefix)),
							ConfigUtil.getString(true, String.format("%s.db.url", prefix)),
							ConfigUtil.getString(true, String.format("%s.db.username", prefix)),
							ConfigUtil.getString(true, String.format("%s.db.password", prefix))
						);
					} else {
						throw new RuntimeException("Module has local persistor but no entities: " + moduleName);
					}
				} else {
					throw new RuntimeException("No local persistor for module: " + moduleName);
				}
			} else {
				if (module.getEntities() != null && module.getEntities().size() > 0) {
					dmtPersistorServiceEntities.addAll(loadEntities(module.getEntities()));
				}
			}
		}

		persistors.get("dmtPersistorService").init(
			dmtPersistorServiceEntities,
			ConfigUtil.getString(true, "dmt.db.driver"),
			ConfigUtil.getString(true, "dmt.db.url"),
			ConfigUtil.getString(true, "dmt.db.username"),
			ConfigUtil.getString(true, "dmt.db.password")
		);
	}

	private static List<Class> loadEntities(List<Entity> entities) {
		List<Class> classes = new ArrayList<>();
		for (Entity entity : entities) {
			try {
				classes.add(Class.forName(entity.getType()));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Entity class not found: " + entity);
			}
		}
		return classes;
	}

	private static void shutdownPersistorServices() {
		Map<String, IPersistorService> persistors = appCtx.getBeansOfType(IPersistorService.class);
		for (IPersistorService persistorService : persistors.values()) {
			persistorService.shutdown();
		}
	}
}
