package org.devocative.demeter.core;

import com.thoughtworks.xstream.XStream;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.IConfigKey;
import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.IDemeterCoreEventListener;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class DemeterCore {
	private static final Logger logger = LoggerFactory.getLogger(DemeterCore.class);

	private final Map<String, XModule> MODULES = new LinkedHashMap<>();
	private final Set<String> MODULE_SHORT_NAMES = new LinkedHashSet<>();

	private ClassPathXmlApplicationContext appCtx;
	private final Map<ApplicationLifecyclePriority, Map<String, IApplicationLifecycle>> APP_LIFECYCLE_BEANS = new HashMap<>();

	private boolean shuted = false;

	private StepResultVO MAIN_STARTUP = new StepResultVO(EStartupStep.Begin);
	private final List<IDemeterCoreEventListener> DEMETER_CORE_EVENTS = new ArrayList<>();

	private Date startUpDate;

	// ------------------------------

	private static DemeterCore INSTANCE;

	public static DemeterCore get() {
		if (INSTANCE == null || INSTANCE.shuted) {
			INSTANCE = new DemeterCore();
		}

		return INSTANCE;
	}

	// ------------------------------

	public void init() {
		init(getDefaultConfig());
	}

	public synchronized void init(InputStream configInputStream) {
		if (MAIN_STARTUP.getStep() == EStartupStep.Begin) {
			ConfigUtil.load(configInputStream);

			MAIN_STARTUP = startUntil(EStartupStep.Begin, EStartupStep.End);

			checkAndExecuteAfterSuccess();
		}
	}

	public void resume() {
		logger.info("## RESUMING");

		if (!isStartedSuccessfully()) {
			MAIN_STARTUP = startUntil(MAIN_STARTUP.getStep(), EStartupStep.End);

			checkAndExecuteAfterSuccess();
		}
	}

	public synchronized void shutdown() {
		if (!shuted) {
			shutdownApplicationLifecycle();
			appCtx.close();
			logger.info("### MODULE LOADER SHUTDOWNED");

			// Make it null for another test case to create an INSTANCE fresh
			INSTANCE = null;
		}

		shuted = true;
	}

	public void registerSpringBean(String beanName, Object bean) {
		ConfigurableListableBeanFactory beanFactory = appCtx.getBeanFactory();
		beanFactory.registerSingleton(beanName, bean);
		beanFactory.autowireBean(bean);
	}

	public void generatePersistorSchemaDiff() {
		generatePersistorSchemaDiff(getDefaultConfig());
	}

	public void generatePersistorSchemaDiff(InputStream configInputStream) {
		ConfigUtil.load(configInputStream);

		StepResultVO resultVO = startUntil(EStartupStep.Begin, EStartupStep.LazyBeans);
		if (!resultVO.isSuccessful()) {
			logger.error("=======================================");
			logger.error("== {}", resultVO.getError());
			logger.error("=======================================");
		}

		Map<String, IPersistorService> persistorServiceMap = appCtx.getBeansOfType(IPersistorService.class);
		for (Map.Entry<String, IPersistorService> entry : persistorServiceMap.entrySet()) {
			logger.info("Persistor init: {}", entry.getKey());
			entry.getValue().init();
		}

		for (Map.Entry<String, IPersistorService> entry : persistorServiceMap.entrySet()) {
			logger.info("Persistor schema diff: {}", entry.getKey());
			entry.getValue().generateSchemaDiff();
		}

		for (Map.Entry<String, IPersistorService> entry : persistorServiceMap.entrySet()) {
			logger.info("Persistor shutdown: {}", entry.getKey());
			entry.getValue().shutdown();
		}

		appCtx.close();
	}

	public void applyAllDbDiffs() {
		startUntil(EStartupStep.Begin, EStartupStep.Database);

		DemeterCoreHelper.initDatabase(new ArrayList<>(MODULE_SHORT_NAMES), true);
	}

	public void applyDbDiffs(List<DbDiffVO> diffs) {
		DemeterCoreHelper.applyDbDiffs(diffs);
	}

	public void addCoreEvent(IDemeterCoreEventListener listener) {
		DEMETER_CORE_EVENTS.add(listener);
	}

	// ---------------

	public ApplicationContext getApplicationContext() {
		return appCtx;
	}

	public Map<String, XModule> getModules() {
		return new LinkedHashMap<>(MODULES);
	}

	public boolean isStartedSuccessfully() {
		return MAIN_STARTUP.getStep() == EStartupStep.End;
	}

	public StepResultVO getLatestStat() {
		return new StepResultVO(MAIN_STARTUP.getStep(), MAIN_STARTUP.getError());
	}

	public List<DbDiffVO> getDbDiffs() {
		return DemeterCoreHelper.getDbDiffs(new ArrayList<>(MODULE_SHORT_NAMES));
	}

	public List<String> getEntities() {
		List<String> list = new ArrayList<>();
		for (Map.Entry<String, XModule> moduleEntry : MODULES.entrySet()) {
			XModule xModule = moduleEntry.getValue();
			if (xModule.getEntities() != null) {
				for (XEntity xEntity : xModule.getEntities()) {
					list.add(xEntity.getType());
				}
			}
		}
		return list;
	}

	public Date getStartUpDate() {
		return startUpDate;
	}

	// ------------------------------

	private StepResultVO startUntil(EStartupStep step, EStartupStep last) {
		StepResultVO result = null;
		while (step.ordinal() < last.ordinal()) {
			logger.info("## Executing Step: [{}]", EStartupStep.next(step));
			result = doNext(step);
			logger.info("## Executed Step: [{}], successful=[{}]", EStartupStep.next(step), result.isSuccessful());
			step = result.getStep();

			if (!result.isSuccessful()) {
				break;
			}
		}
		return result != null ? result : new StepResultVO(step);
	}

	private StepResultVO doNext(EStartupStep current) {
		Exception error = null;
		EStartupStep next = EStartupStep.next(current);

		try {
			switch (next) {
				case Begin:
					break;

				case EncDec:
					initEncDec();
					break;

				case Modules:
					initModules();
					break;

				case Spring:
					initSpringContext();
					break;

				case PersistenceServices:
					initPersistorServices();
					break;

				case Database:
					initDatabase();
					break;

				case LazyBeans:
					initLazyBeans();
					break;

				case BeansStartup:
					initBeansStartup();
					break;

				case End:
					initEnd();
					break;

				default:
					throw new DSystemException("Unhandled Step: " + current);
			}
		} catch (Exception e) {
			logger.warn("!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			logger.warn("!! Step = [{}]", next, e);
			logger.warn("!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			error = e;
		}

		return new StepResultVO(next, error);
	}

	private void checkAndExecuteAfterSuccess() {
		if (isStartedSuccessfully()) {
			for (IDemeterCoreEventListener demeterUp : DEMETER_CORE_EVENTS) {
				demeterUp.afterUpSuccessfully();
			}
			logger.info("### MODULE LOADER INITED");
		}
	}

	// --------------- STEPS

	private void initEncDec() {
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

	private void initModules() {
		XStream xStream = new XStream();
		xStream.processAnnotations(XModule.class);
		xStream.alias("dependency", String.class);

		List<String> modulesName = ConfigUtil.getList(DemeterConfigKey.Modules);
		if (!modulesName.contains("Demeter")) {
			modulesName.add(0, "Demeter");
		}

		for (String moduleName : modulesName) {
			InputStream moduleXMLResource = DemeterCore.class.getResourceAsStream(String.format("/%s.xml", moduleName));
			XModule xModule = (XModule) xStream.fromXML(moduleXMLResource);
			logger.info("Module Found: {}", moduleName);

			if (MODULE_SHORT_NAMES.contains(xModule.getShortName())) {
				throw new DSystemException("Duplicate module short name: " + xModule.getShortName());
			}
			MODULE_SHORT_NAMES.add(xModule.getShortName());

			MODULES.put(moduleName, xModule);

			loadConfigKeys(xModule);
		}
	}

	private void initSpringContext() {
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

	private void initPersistorServices() {
		Map<String, IPersistorService> persistors = appCtx.getBeansOfType(IPersistorService.class);

		List<Class> dmtPersistorServiceEntities = new ArrayList<>();
		for (Map.Entry<String, XModule> moduleEntry : MODULES.entrySet()) {
			XModule module = moduleEntry.getValue();
			/* TODO
			String moduleName = moduleEntry.getKey();
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
			} else {*/
			if (module.getEntities() != null && module.getEntities().size() > 0) {
				dmtPersistorServiceEntities.addAll(loadEntities(module.getEntities()));
				logger.info("Module has {} entities.", module.getEntities().size());
			}
			//}
		}

		persistors.get("dmtPersistorService").setInitData(dmtPersistorServiceEntities, "dmt");
		logger.info("Demeter persistor initialized with [{}] entities.", dmtPersistorServiceEntities.size());
	}

	private void initDatabase() {
		DemeterCoreHelper.initDatabase(new ArrayList<>(MODULE_SHORT_NAMES), false);
	}

	private void initLazyBeans() {
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

	private void initBeansStartup() {
		Map<String, IApplicationLifecycle> beansOfType = appCtx.getBeansOfType(IApplicationLifecycle.class);
		for (Map.Entry<String, IApplicationLifecycle> entry : beansOfType.entrySet()) {
			if (entry.getValue().getLifecyclePriority() == null) {
				throw new DSystemException("IApplicationLifecycle has no priority: " + entry.getKey());
			}

			if (!APP_LIFECYCLE_BEANS.containsKey(entry.getValue().getLifecyclePriority())) {
				APP_LIFECYCLE_BEANS.put(entry.getValue().getLifecyclePriority(), new HashMap<>());
			}

			APP_LIFECYCLE_BEANS
				.get(entry.getValue().getLifecyclePriority())
				.put(entry.getKey(), entry.getValue());
		}

		for (ApplicationLifecyclePriority priority : ApplicationLifecyclePriority.values()) {
			Map<String, IApplicationLifecycle> lifecycleMap = APP_LIFECYCLE_BEANS.get(priority);
			for (Map.Entry<String, IApplicationLifecycle> entry : lifecycleMap.entrySet()) {
				entry.getValue().init();
				logger.info("Application Lifecycle Priority [{}] init(): bean=[{}]", priority, entry.getKey());
			}
		}

		Map<String, IPersistorService> iPersistorServiceMap = appCtx.getBeansOfType(IPersistorService.class);
		iPersistorServiceMap.values().forEach(org.devocative.demeter.iservice.persistor.IPersistorService::endSession);
	}

	private void initEnd() {
		if (ConfigUtil.hasKey(DemeterConfigKey.StartupGroovyScript)) {
			File file = new File(ConfigUtil.getString(DemeterConfigKey.StartupGroovyScript));
			if (file.exists()) {
				try {
					GroovyShell shell = new GroovyShell();
					Script parse = shell.parse(file);
					parse.run();

					logger.info("Script executed successfully: [{}]",
						ConfigUtil.getString(DemeterConfigKey.StartupGroovyScript));
				} catch (Exception e) {
					logger.error("initEnd: script file = [{}]",
						ConfigUtil.getString(DemeterConfigKey.StartupGroovyScript), e);
				}
			} else {
				logger.error("Script file not found: " + ConfigUtil.getString(DemeterConfigKey.StartupGroovyScript));
			}
		}

		startUpDate = new Date();
	}

	// ---------------

	private void shutdownApplicationLifecycle() {
		List<ApplicationLifecyclePriority> priorities = Arrays.asList(ApplicationLifecyclePriority.values());
		Collections.reverse(priorities);

		for (ApplicationLifecyclePriority priority : priorities) {
			Map<String, IApplicationLifecycle> lifecycleMap = APP_LIFECYCLE_BEANS.get(priority);
			for (Map.Entry<String, IApplicationLifecycle> entry : lifecycleMap.entrySet()) {
				entry.getValue().shutdown();
				logger.info("Application Lifecycle Priority [{}] shutdown(): bean=[{}]", priority, entry.getKey());
			}
		}
	}

	// ---------------

	private List<Class> loadEntities(List<XEntity> entities) {
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

	private void loadConfigKeys(XModule xModule) {
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

	private InputStream getDefaultConfig() {
		return DemeterCore.class.getResourceAsStream("/config.properties");
	}
}