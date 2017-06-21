package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.obuilder.MapBuilder;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.ELockMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.hibernate.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Statement;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class HibernatePersistorService implements IPersistorService {
	private static final Logger logger = LoggerFactory.getLogger(HibernatePersistorService.class);
	private static final ThreadLocal<Session> currentSession = new ThreadLocal<>();

	private List<Class> entities;
	private String prefix;

	private SessionFactory sessionFactory;
	private Metadata metaData;

	@Autowired
	private ISecurityService securityService;

	// -------------------------- IApplicationLifecycle implementation

	@Override
	public void init() {
		String username = ConfigUtil.getString(true, getConfig("db.username"));

		MapBuilder<String, Object> settingsBuilder = new MapBuilder<String, Object>(new HashMap<>())
			.put("hibernate.dialect", ConfigUtil.getString(true, getConfig("db.dialect")))
			.put("hibernate.connection.driver_class", ConfigUtil.getString(true, getConfig("db.driver")))
			.put("hibernate.connection.url", ConfigUtil.getString(true, getConfig("db.url")))
			.put("hibernate.connection.username", username)
			.put("hibernate.connection.password", ConfigUtil.getString(getConfig("db.password"), ""))
			.put("hibernate.show_sql", ConfigUtil.getString(getConfig("db.showSQL"), "false"));

		String schema = ConfigUtil.getString(false, getConfig("db.schema"));
		if (schema != null) {
			settingsBuilder.put("hibernate.default_schema", schema);
		}

		StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
		StandardServiceRegistry serviceRegistry = serviceRegistryBuilder
			.applySettings(settingsBuilder.get())
			.configure("hibernate.cfg.xml")
			.build();
		MetadataSources metadataSources = new MetadataSources(serviceRegistry);
		entities.forEach(metadataSources::addAnnotatedClass);
		metaData = metadataSources.buildMetadata();
		SessionFactoryBuilder sessionFactoryBuilder = metaData.getSessionFactoryBuilder();

		String interceptor = ConfigUtil.getString(getConfig("db.interceptor"), "CreateModify");
		if ("CreateModify".equals(interceptor)) {
			sessionFactoryBuilder.applyInterceptor(new MainInterceptor());
		} else {
			logger.warn("HibernatePersistorService without CreateModifyInterceptor!");
		}

		sessionFactory = sessionFactoryBuilder.build();

		logger.info("HibernatePersistorService init()");

		String scriptFile = ConfigUtil.getString(getConfig("db.script"), null);
		if (scriptFile != null) {
			logger.info("Executing script file: {}", scriptFile);

			try {
				File f = new File(scriptFile);
				String sql = new String(Files.readAllBytes(f.toPath()));
				executeScript(sql, ";");
			} catch (Exception e) {
				logger.error("Executing script file: ", e);
			}
		}
	}

	@Override
	public void shutdown() {
		endSession();
		sessionFactory.close();
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.First;
	}

	// -------------------------- IRequestLifecycle implementation

	@Override
	public void beforeRequest() {
	}

	@Override
	public void afterResponse() {
		endSession();
	}

	// -------------------------- IPersistorService implementation

	@Override
	public void setInitData(List<Class> entities, String prefix) {
		this.entities = entities;
		this.prefix = prefix;
	}

	@Override
	public void commitOrRollback() {
		Session session = currentSession.get();
		if (session != null && session.isOpen()) {
			Transaction trx = session.getTransaction();
			try {
				if (trx.isActive()) {
					trx.commit();
				}
			} catch (HibernateException e) {
				logger.error("Hibernate endSession: ", e);
				trx.rollback();
			}
		}
	}

	@Override
	public void rollback() {
		Session session = currentSession.get();
		if (session != null && session.isOpen()) {
			Transaction trx = session.getTransaction();
			if (trx != null && trx.isActive()) {
				trx.rollback();
			}
		}
	}

	@Override
	public void endSession() {
		commitOrRollback();

		Session session = currentSession.get();
		if (session != null && session.isOpen()) {
			session.close();
		}
		currentSession.remove();
	}

	@Override
	public void saveOrUpdate(Object obj) {
		Session session = getCurrentSession();
		checkTransaction(session);
		session.saveOrUpdate(obj);
		session.flush();
	}

	@Override
	public void save(Object obj) {
		Session session = getCurrentSession();
		checkTransaction(session);
		session.save(obj);
		session.flush();
	}

	@Override
	public void update(Object obj) {
		Session session = getCurrentSession();
		checkTransaction(session);
		session.update(obj);
		session.flush();
	}

	@Override
	public void merge(Object obj) {
		Session session = getCurrentSession();
		checkTransaction(session);
		session.merge(obj);
		session.flush();
	}

	@Override
	public <T> T get(Class<T> entity, Serializable id) {
		return getCurrentSession().get(entity, id);
	}

	@Override
	public <T> T load(Class<T> entity, Serializable id, ELockMode lockMode) {
		LockOptions lockOptions = LockOptions.NONE;
		switch (lockMode) {
			case READ:
				lockOptions = LockOptions.READ;
				break;
			case UPGRADE:
				lockOptions = LockOptions.UPGRADE;
				break;
		}
		return getCurrentSession().load(entity, id, lockOptions);
	}

	@Override
	public void delete(Class entity, Serializable id) {
		Session session = getCurrentSession();
		checkTransaction(session);
		String q = String.format("delete from %s ent where ent.id=:entId", entity.getName());
		Query query = session.createQuery(q);
		query.setParameter("entId", id);
		query.executeUpdate();
	}

	@Override
	public void delete(Object obj) {
		Session session = getCurrentSession();
		checkTransaction(session);
		session.delete(obj);
	}

	@Override
	public <T> List<T> list(Class<T> entity) {
		return list(String.format("from %s ent", entity.getName()));
	}

	@Override
	public <T> List<T> list(String simpleQuery) {
		Session session = getCurrentSession();
		Query<T> query = session.createQuery(simpleQuery);
		return query.list();
	}

	@Override
	public void refresh(Object entity) {
		try {
			getCurrentSession().refresh(entity);
		} catch (HibernateException xcp) {
			logger.info(xcp.getMessage());
		}
	}

	@Override
	public void executeUpdate(String simpleQuery) {
		Session session = getCurrentSession();
		checkTransaction(session);
		session.createQuery(simpleQuery).executeUpdate();
		session.flush();
	}

	@Override
	public IQueryBuilder createQueryBuilder() {
		return new HibernateQueryBuilder(this);
	}

	@Override
	public void generateSchemaDiff() {
		SchemaUpdate schemaUpdate = new SchemaUpdate();
		schemaUpdate.setDelimiter(";");
		schemaUpdate.setFormat(true);
		schemaUpdate.execute(EnumSet.of(TargetType.STDOUT), metaData);
	}

	@Override
	public void executeScript(final String script, final String delimiter) {
		Session session = getSession();
		session.doWork(connection -> {
			String[] statements = script.split(delimiter);
			for (String statement : statements) {
				statement = statement.trim();
				if (!statement.isEmpty()) {
					logger.info("Execute: {}", statement);
					Statement st = connection.createStatement();
					st.execute(statement);
					st.close();
				}
			}
		});
		session.close();
	}

	//----------------------------- PACKAGE METHODS

	Session getCurrentSession() {
		Session session = currentSession.get();
		if (session == null || !session.isOpen()) {
			session = getSession();
			currentSession.set(session);
		} else {
			try {
				session.createNativeQuery(ConfigUtil.getString(true, getConfig("db.connection.check.query"))).uniqueResult();
			} catch (HibernateException e) {
				logger.warn("Check current session error", e);
				currentSession.remove();
				session = getCurrentSession();
			}
		}
		return session;
	}

	void checkTransaction(Session session) {
		Transaction trx = session.getTransaction();
		if (trx == null || !trx.isActive()) {
			session.beginTransaction();
		}
	}

	//----------------------------- PRIVATE METHODS

	private Session getSession() {
		int noOfRetry = 0;
		while (true) {
			try {
				Session session = sessionFactory.openSession();
				session.createNativeQuery(ConfigUtil.getString(true, getConfig("db.connection.check.query"))).uniqueResult();
				return session;
			} catch (HibernateException e) {
				logger.error("Problem getting new session", e);
				try {
					sessionFactory.close();
				} catch (HibernateException e1) {
					logger.warn("Problem closing SessionFactory", e);
				}
				noOfRetry++;
				if (noOfRetry > 3) {
					//TODO SMSUtil.sendSMS("hibernate.connection");
					throw e;
				}

				init();
			}
		}
	}

	private String getConfig(String key) {
		return String.format("%s.%s", prefix, key);
	}

	//----------------------------- INNER CLASSES

	private class MainInterceptor extends EmptyInterceptor {
		private static final long serialVersionUID = -820555101887857570L;

		// insert
		public boolean onSave(
			Object entity,
			Serializable id,
			Object[] state,
			String[] propertyNames,
			Type[] types) {

			if (entity instanceof ICreationDate ||
				entity instanceof ICreatorUser ||
				entity instanceof IRowMod) {
				setCreatedValues(entity, id, state, propertyNames);
				return true;
			}

			if (entity instanceof IModificationDate || entity instanceof IModifierUser) {
				setModifiedValues(entity, id, state, propertyNames);
				return true;
			}

			return false;
		}

		// update
		public boolean onFlushDirty(
			Object entity,
			Serializable id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			Type[] types) {

			if (entity instanceof IModificationDate ||
				entity instanceof IModifierUser ||
				entity instanceof IRowMod) {
				setModifiedValues(entity, id, currentState, propertyNames);
				return true;
			}

			return false;
		}

		private void setCreatedValues(Object entity, Serializable id, Object[] state, String[] propertyNames) {
			for (int i = 0; i < propertyNames.length; i++) {
				if ("creatorUserId".equals(propertyNames[i])) {
					if (securityService != null && securityService.getCurrentUser() != null) {
						state[i] = securityService.getCurrentUser().getUserId();
					} else {
						logger.error("Hibernate.Interceptor for creatorUserId: invalid currentUser, entity=[{}] id=[{}]", entity.getClass().getName(), id);
						if (entity instanceof Person) {
							Person p = (Person) entity;
							if (!"system".equals(p.getLastName())) {
								throw new RuntimeException("Invalid CurrentUser");
							}
						} else {
							throw new RuntimeException("Invalid CurrentUser");
						}
					}
				} else if ("creationDate".equals(propertyNames[i])) {
					state[i] = new Date();
				} else if ("rowMod".equals(propertyNames[i])) {
					if (state[i] == null) {
						state[i] = ERowMod.NORMAL;
					}
				}
			}
		}

		private void setModifiedValues(Object entity, Serializable id, Object[] state, String[] propertyNames) {
			for (int i = 0; i < propertyNames.length; i++) {
				if ("modifierUserId".equals(propertyNames[i])) {
					if (securityService != null && securityService.getCurrentUser() != null) {
						state[i] = securityService.getCurrentUser().getUserId();
					} else {
						logger.error("Hibernate.Interceptor for creatorUserId: invalid currentUser, entity=[{}] id=[{}]", entity.getClass().getName(), id);
						if (entity instanceof Person) {
							Person p = (Person) entity;
							if (!"system".equals(p.getLastName())) {
								throw new RuntimeException("Invalid CurrentUser");
							}
						} else {
							throw new RuntimeException("Invalid CurrentUser");
						}
					}
				} else if ("modificationDate".equals(propertyNames[i])) {
					state[i] = new Date();
				} else if ("rowMod".equals(propertyNames[i])) {
					if (state[i] == null) {
						state[i] = ERowMod.NORMAL;
					}
				}
			}
		}
	}
}
