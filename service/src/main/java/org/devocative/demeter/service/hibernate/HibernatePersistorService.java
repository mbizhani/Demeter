package org.devocative.demeter.service.hibernate;

import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.ObjectUtil;
import org.devocative.demeter.DBConstraintViolationException;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.ELockMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HibernatePersistorService implements IPersistorService {
	private static final Logger logger = LoggerFactory.getLogger(HibernatePersistorService.class);

	private static final ThreadLocal<Session> currentSession = new ThreadLocal<>();
	private static final ThreadLocal<AtomicInteger> currentTrxLevel = new ThreadLocal<>();

	private static final Pattern HSQLDB_CONSTRAINT_NAME = Pattern.compile("unique constraint or index violation; (.+?) table:");

	private List<Class> entities;
	private String prefix;

	private Configuration config;
	private SessionFactory sessionFactory;

	@Autowired
	private ISecurityService securityService;

	// -------------------------- IApplicationLifecycle

	@Override
	public void init() {
		config = new Configuration();
		entities.forEach(config::addAnnotatedClass);

		String interceptor = ConfigUtil.getString(getConfig("db.interceptor"), "CreateModify");
		if ("CreateModify".equals(interceptor)) {
			config.setInterceptor(new HibernateInterceptor(securityService));
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

		Boolean applyDDL = ConfigUtil.getBoolean(getConfig("db.update.ddl"), false);
		if (applyDDL) {
			config.setProperty("hibernate.hbm2ddl.auto", "update");
		}

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

	// --------------- IRequestLifecycle

	@Override
	public void beforeRequest() {
	}

	@Override
	public void afterResponse() {
		endSession();
	}

	// --------------- IPersistorService

	@Override
	public void setInitData(List<Class> entities, String prefix) {
		this.entities = entities;
		this.prefix = prefix;
	}

	// --------------- Trx Handling

	@Override
	public void startTrx() {
		startTrx(false);
	}

	@Override
	public void startTrx(boolean forceNew) {
		final Transaction trx = getCurrentTrx();
		final int level = currentTrxLevel.get().incrementAndGet();

		if (trx.isActive()) {
			if (level == 0) {
				throw new DemeterException(DemeterErrorCode.TrxInvalidLevel,
					String.format("level = %s, active = %s", level, trx.isActive()));
			}

			if (forceNew) {
				throw new DemeterException(DemeterErrorCode.TrxAlreadyActiveNoForce);
			}
		} else {
			if (level > 1) {
				throw new DemeterException(DemeterErrorCode.TrxInvalidLevel,
					String.format("level = %s, active = %s", level, trx.isActive()));
			}

			trx.begin();
		}
	}

	@Override
	public void assertActiveTrx() {
		final int level = currentTrxLevel.get().get();
		final Transaction trx = getCurrentTrx();

		if (!trx.isActive() || level == 0) {
			throw new DemeterException(DemeterErrorCode.TrxNotActive,
				String.format("level = %s, active = %s", level, trx.isActive()));
		}
	}

	@Override
	public void commitOrRollback() {
		assertActiveTrx();

		final Transaction trx = getCurrentTrx();
		final AtomicInteger trxLevel = currentTrxLevel.get();

		try {
			if (trxLevel.decrementAndGet() == 0) {
				trx.commit();
			}
		} catch (Exception e) {
			logger.error("Hibernate commitOrRollback(): ", e);
			trx.rollback();
			throw e;
		}
	}

	@Override
	public void rollback() {
		//assertActiveTrx();

		currentTrxLevel.get().set(0);

		final Transaction trx = getCurrentTrx();
		if (trx.isActive()) {
			trx.rollback();
		}
	}

	@Override
	public void endSession() {
		final Session session = currentSession.get();
		final int level = currentTrxLevel.get() != null ? currentTrxLevel.get().get() : 0;

		try {
			if (session != null && session.isOpen()) {
				final Transaction trx = session.getTransaction();

				if (trx.isActive()) {
					trx.rollback();

					throw new DemeterException(DemeterErrorCode.TrxInvalidActive);

				} else if (level > 0) {
					throw new DemeterException(DemeterErrorCode.TrxInvalidLevel, "level = " + level);
				}
			}
		} finally {
			currentTrxLevel.remove();
			currentSession.remove();

			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	// --------------- @Transactional

	@Transactional
	@Override
	public void saveOrUpdate(Object obj) {
		//onBeforeInsertOrUpdate(obj);
		//EntityResetInfo info = new EntityResetInfo(obj);

		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			//boolean chk = checkTransaction(session);
			session.saveOrUpdate(obj);
			session.flush();
			//processTransaction(chk);
		} catch (ConstraintViolationException e) {
			//rollback();
			//info.reset();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	@Transactional
	@Override
	public Serializable save(Object obj) {
		//onBeforeInsertOrUpdate(obj);

		assertActiveTrx();

		try {
			final Session session = getCurrentSession();

			//boolean chk = checkTransaction(session);
			Serializable result = session.save(obj);
			session.flush();
			//processTransaction(chk);

			return result;
		} catch (ConstraintViolationException e) {
			//rollback();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	@Transactional
	@Override
	public void update(Object obj) {
		//onBeforeInsertOrUpdate(obj);

		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			//boolean chk = checkTransaction(session);
			session.update(obj);
			session.flush();
			//processTransaction(chk);
		} catch (ConstraintViolationException e) {
			//rollback();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	@Transactional
	@Override
	public Object updateFields(Object obj, String... fields) {
		if (fields != null && fields.length > 0) {
			assertActiveTrx();

			final Class cls = HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
			final ClassMetadata classMetadata = sessionFactory.getClassMetadata(cls);
			final String idPropName = classMetadata.getIdentifierPropertyName();
			final Object idPropValue = ObjectUtil.getPropertyValue(obj, idPropName, false);

			final StringBuilder builder = new StringBuilder();
			builder
				.append("update ")
				.append(cls.getName())
				.append(" ent set ent.").append(fields[0]).append(" = :f0");

			for (int i = 1; i < fields.length; i++) {
				builder.append(", ent.").append(fields[i]).append(" = :f").append(i);
			}

			builder.append(" where ent.").append(idPropName).append(" = :id");

			logger.debug("HibernatePersistorService.updateFields Query: {}", builder.toString());

			try {
				final Session session = getCurrentSession();
				//boolean chk = checkTransaction(session);
				final Query query = session.createQuery(builder.toString());
				for (int i = 0; i < fields.length; i++) {
					query.setParameter("f" + i, ObjectUtil.getPropertyValue(obj, fields[i], false));
				}
				query.setParameter("id", idPropValue);
				query.executeUpdate();
				session.flush();
				//processTransaction(chk);
			} catch (ConstraintViolationException e) {
				//rollback();
				throw new DBConstraintViolationException(getConstraintName(e));
			}

			return idPropValue;
		} else {
			throw new RuntimeException("No filed!");
		}
	}

	@Transactional
	@Override
	public void persist(Object obj) {
		//onBeforeInsertOrUpdate(obj);

		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			//boolean chk = checkTransaction(session);
			session.persist(obj);
			session.flush();
			//processTransaction(chk);
		} catch (ConstraintViolationException e) {
			//rollback();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	@Transactional
	@Override
	public <T> T merge(T obj) {
		//onBeforeInsertOrUpdate(obj);

		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			//boolean chk = checkTransaction(session);

			final T result = (T) session.merge(obj);
			session.flush();
			//processTransaction(chk);

			return result;
		} catch (ConstraintViolationException e) {
			//rollback();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	@Transactional
	@Override
	public void delete(Class entity, Serializable id) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			//boolean chk = checkTransaction(session);
			final String q = String.format("delete from %s ent where ent.id=:entId", entity.getName());
			final Query query = session.createQuery(q);
			query.setParameter("entId", id);
			query.executeUpdate();
			session.flush();
			//processTransaction(chk);
		} catch (ConstraintViolationException e) {
			//rollback();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	@Transactional
	@Override
	public void delete(Object obj) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			//boolean chk = checkTransaction(session);
			session.delete(obj);
			session.flush();
			//processTransaction(chk);
		} catch (ConstraintViolationException e) {
			//rollback();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	@Transactional
	@Override
	public void executeUpdate(String simpleQuery) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			//boolean chk = checkTransaction(session);
			session.createQuery(simpleQuery).executeUpdate();
			session.flush();
			//processTransaction(chk);
		} catch (ConstraintViolationException e) {
			//rollback();
			throw new DBConstraintViolationException(getConstraintName(e));
		}
	}

	// ---------------

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

	// ------------------------------

	Session getCurrentSession() {
		Session session = currentSession.get();
		if (session == null || !session.isOpen()) {
			session = getSession();
			currentSession.set(session);
			currentTrxLevel.set(new AtomicInteger(0));
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

	Transaction getCurrentTrx() {
		final Session session = getCurrentSession();
		final Transaction trx = session.getTransaction();

		if (trx == null) {
			throw new DemeterException(DemeterErrorCode.TrxNoObject);
		}

		return trx;
	}

	boolean checkTransaction(Session session) {
		Transaction trx = session.getTransaction();
		if (!trx.isActive()) {
			session.beginTransaction();
			return true;
		}
		return false;
	}

	void processTransaction(boolean check) {
		if (check) {
			Session session = getCurrentSession();
			if (session != null && session.isOpen()) {
				Transaction trx = session.getTransaction();
				if (trx.isActive()) {
					trx.commit();
				}
			}
		}
	}

	String getConstraintName(ConstraintViolationException e) {
		if (e.getConstraintName() != null) {
			return e.getConstraintName();
		} else {
			Matcher matcher = HSQLDB_CONSTRAINT_NAME.matcher(e.getSQLException().getMessage());
			if (matcher.find()) {
				return matcher.group(1);
			}
		}

		return null;
	}

	// ------------------------------

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

	/*private void onBeforeInsertOrUpdate(Object entity) {
		UserVO currentUser = securityService.getCurrentUser();

		//TODO the following code must be placed in HibernateInterceptor
		if (entity instanceof IRowMode && entity instanceof IRoleRowAccess && !currentUser.isAdmin()) {
			IRowMode rowMode = (IRowMode) entity;
			IRoleRowAccess roleRowAccess = (IRoleRowAccess) entity;

			if (ERowMode.ROLE.equals(rowMode.getRowMode())) {
				if (roleRowAccess.getAllowedRoles() == null) {
					roleRowAccess.setAllowedRoles(new ArrayList<>());
				}
				List<Role> roles = roleRowAccess.getAllowedRoles();

				if (Collections.disjoint(currentUser.getRoles(), roles)) {
					Role currentUserMainRole = null;
					for (Role role : currentUser.getRoles()) {
						if (ERoleMode.MAIN.equals(role.getRoleMode())) {
							currentUserMainRole = role;
							break;
						}
					}

					if (currentUserMainRole != null) {
						if (!roles.contains(currentUserMainRole)) {
							roles.add(currentUserMainRole);
						}
					} else {
						throw new DemeterException(DemeterErrorCode.NoMainRoleForUser);
					}
				}
			}
		}
	}

	private class EntityResetInfo {
		private Object object;
		private String idPropName;
		private Object idPropValue;

		private Integer ver = null;
		private IModificationDate modificationDate = null;

		public EntityResetInfo(Object object) {
			this.object = object;

			Class cls = HibernateProxyHelper.getClassWithoutInitializingProxy(object);
			ClassMetadata classMetadata = sessionFactory.getClassMetadata(cls);

			idPropName = classMetadata.getIdentifierPropertyName();
			idPropValue = ObjectUtil.getPropertyValue(object, idPropName, false);

			if (object instanceof IModificationDate) {
				modificationDate = (IModificationDate) object;
				ver = modificationDate.getVersion();
			}
		}

		public void reset() {
			if (idPropValue == null) {
				ObjectUtil.setPropertyValue(object, idPropName, null, false);
			}
			if (modificationDate != null) {
				modificationDate.setVersion(ver);
			}
		}
	}*/
}
