package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.ELockMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

public class HibernatePersistorService implements IPersistorService {
	private static final Logger logger = LoggerFactory.getLogger(HibernatePersistorService.class);
	private static final ThreadLocal<Session> currentSession = new ThreadLocal<>();

	private List<Class> entities;
	private String prefix;

	private Configuration config;
	private SessionFactory sessionFactory;

	@Autowired
	private ISecurityService securityService;

	// -------------------------- IApplicationLifecycle implementation

	@Override
	public void init() {
		config = new Configuration();
		entities.forEach(config::addAnnotatedClass);

		String interceptor = ConfigUtil.getString(getConfig("db.interceptor"), "CreateModify");
		if ("CreateModify".equals(interceptor)) {
			config.setInterceptor(new MainInterceptor());
		} else {
			logger.warn("HibernatePersistorService without CreateModifyInterceptor!");
		}

		config.configure("hibernate.cfg.xml")
			.setProperty("hibernate.dialect", ConfigUtil.getString(true, getConfig("db.dialect")))
			.setProperty("hibernate.connection.driver_class", ConfigUtil.getString(true, getConfig("db.driver")))
			.setProperty("hibernate.connection.url", ConfigUtil.getString(true, getConfig("db.url")))
			.setProperty("hibernate.connection.username", ConfigUtil.getString(true, getConfig("db.username")))
			.setProperty("hibernate.connection.password", ConfigUtil.getString(getConfig("db.password"), ""))
			.setProperty("hibernate.show_sql", ConfigUtil.getString(getConfig("db.showSQL"), "false"));

		String schema = ConfigUtil.getString(false, getConfig("db.schema"));
		if (schema != null) {
			config.setProperty("hibernate.default_schema", schema);
		}

		/*Boolean applyDDL = ConfigUtil.getBoolean(getConfig("db.apply.ddl"), false);
		if (applyDDL) {
			config.setProperty("hibernate.hbm2ddl.auto", "update");
		}*/

		// In Hibernate 4.3:
		StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
		serviceRegistryBuilder.applySettings(config.getProperties());
		sessionFactory = config.buildSessionFactory(serviceRegistryBuilder.build());
		logger.info("HibernatePersistorService init(db = {})", ConfigUtil.getString(true, getConfig("db.username")));

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
		try {
			endSession();
		} catch (Exception e) {
			logger.error("HibernatePersistorService.shutdown(): endSession", e);
		}

		try {
			sessionFactory.close();
		} catch (HibernateException e) {
			logger.error("HibernatePersistorService.shutdown(): sessionFactory.close()", e);
		}

		currentSession.remove();
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
		return (T) getCurrentSession().get(entity, id);
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
		return (T) getCurrentSession().load(entity, id, lockOptions);
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
		Query query = session.createQuery(simpleQuery);
		return (List<T>) query.list();
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
		SchemaUpdate schemaUpdate = new SchemaUpdate(config);
		schemaUpdate.setDelimiter(";");
		schemaUpdate.setFormat(true);
		schemaUpdate.execute(true, false);
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

	@Override
	public Connection createSqlConnection() throws SQLException {
		try {
			Class.forName(ConfigUtil.getString(true, getConfig("db.driver")));
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}

		return DriverManager.getConnection(
			ConfigUtil.getString(true, getConfig("db.url")),
			ConfigUtil.getString(true, getConfig("db.username")),
			ConfigUtil.getString(getConfig("db.password"), ""));
	}

	//----------------------------- PACKAGE METHODS

	Session getCurrentSession() {
		Session session = currentSession.get();
		if (session == null || !session.isOpen()) {
			session = getSession();
			currentSession.set(session);
		} else {
			try {
				session.createSQLQuery(ConfigUtil.getString(true, getConfig("db.connection.check.query"))).uniqueResult();
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
				session.createSQLQuery(ConfigUtil.getString(true, getConfig("db.connection.check.query"))).uniqueResult();
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
