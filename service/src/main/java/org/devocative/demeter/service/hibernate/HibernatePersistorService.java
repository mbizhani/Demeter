package org.devocative.demeter.service.hibernate;

import org.apache.commons.beanutils.PropertyUtils;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.ObjectUtil;
import org.devocative.adroit.obuilder.MapBuilder;
import org.devocative.demeter.DBConstraintViolationException;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
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
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.query.Query;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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

	private Metadata metaData;
	private SessionFactory sessionFactory;

	@Autowired
	private ISecurityService securityService;

	// -------------------------- IApplicationLifecycle

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

		Boolean applyDDL = ConfigUtil.getBoolean(getConfig("db.update.ddl"), false);
		if (applyDDL) {
			settingsBuilder.put("hibernate.hbm2ddl.auto", "update");
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
			sessionFactoryBuilder.applyInterceptor(new HibernateInterceptor(securityService));
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
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			final Object result = session.merge(obj);
			session.flush();

			copyProperties(result, obj);
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}
	}

	@Transactional
	@Override
	public Serializable save(Object obj) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			final Serializable result = session.save(obj);
			session.flush();
			return result;
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}

		return null;
	}

	@Transactional
	@Override
	public void update(Object obj) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			session.update(obj);
			session.flush();
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}
	}

	@Transactional
	@Override
	public Object updateFields(Object obj, String... fields) {
		if (fields != null && fields.length > 0) {
			assertActiveTrx();

			final Class cls = HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
			final String idPropName = metaData.getIdentifierPropertyName(cls.getName());
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
				final Query query = session.createQuery(builder.toString());
				for (int i = 0; i < fields.length; i++) {
					query.setParameter("f" + i, ObjectUtil.getPropertyValue(obj, fields[i], false));
				}
				query.setParameter("id", idPropValue);
				query.executeUpdate();
				session.flush();
			} catch (PersistenceException e) {
				processPersistenceException(e);
			}

			return idPropValue;
		} else {
			throw new DSystemException("No Field Passed!");
		}
	}

	@Transactional
	@Override
	public void persist(Object obj) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			session.persist(obj);
			session.flush();
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}
	}

	@Transactional
	@Override
	public <T> T merge(T obj) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			final T result = (T) session.merge(obj);
			session.flush();
			return result;
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}

		return null;
	}

	@Transactional
	@Override
	public void delete(Class entity, Serializable id) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			final String q = String.format("delete from %s ent where ent.id=:entId", entity.getName());
			final Query query = session.createQuery(q);
			query.setParameter("entId", id);
			query.executeUpdate();
			session.flush();
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}
	}

	@Transactional
	@Override
	public void delete(Object obj) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			session.delete(obj);
			session.flush();
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}
	}

	@Transactional
	@Override
	public void executeUpdate(String simpleQuery) {
		assertActiveTrx();

		try {
			final Session session = getCurrentSession();
			session.createQuery(simpleQuery).executeUpdate();
			session.flush();
		} catch (PersistenceException e) {
			processPersistenceException(e);
		}
	}

	// ---------------

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
	public <T> List<T> list(Class<T> entity) {
		return list(String.format("from %s ent", entity.getName()));
	}

	@Override
	public <T> List<T> list(String simpleQuery) {
		Session session = getCurrentSession();
		Query query = session.createQuery(simpleQuery);
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
	public IQueryBuilder createQueryBuilder() {
		return new HibernateQueryBuilder(this);
	}

	@Override
	public void generateSchemaDiff() {
		new SchemaUpdate()
			.setDelimiter(";")
			.setFormat(true)
			.execute(EnumSet.of(TargetType.STDOUT), metaData);
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
				session.createNativeQuery(ConfigUtil.getString(true, getConfig("db.connection.check.query"))).uniqueResult();
			} catch (HibernateException e) {
				logger.warn("Check current session error", e);
				currentSession.remove();
				session = getCurrentSession();
			}
		}
		return session;
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

	void processPersistenceException(PersistenceException e) throws PersistenceException {
		if (e.getCause() instanceof ConstraintViolationException) {
			ConstraintViolationException cve = (ConstraintViolationException) e.getCause();
			throw new DBConstraintViolationException(getConstraintName(cve));
		}

		throw e;
	}

	// ------------------------------

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

	private Transaction getCurrentTrx() {
		final Session session = getCurrentSession();
		final Transaction trx = session.getTransaction();

		if (trx == null) {
			throw new DemeterException(DemeterErrorCode.TrxNoObject);
		}

		return trx;
	}

	private String getConfig(String key) {
		return String.format("%s.%s", prefix, key);
	}

	private void copyProperties(Object src, Object dest) {
		try {
			Map<String, PropertyDescriptor> srcProps = getPropertyDescriptorsAsMap(HibernateProxyHelper.getClassWithoutInitializingProxy(src));
			Map<String, PropertyDescriptor> destProps = getPropertyDescriptorsAsMap(HibernateProxyHelper.getClassWithoutInitializingProxy(dest));

			for (Map.Entry<String, PropertyDescriptor> entry : srcProps.entrySet()) {
				final String srcPropName = entry.getKey();

				final PropertyDescriptor srcProp = entry.getValue();
				final PropertyDescriptor destProp = destProps.get(srcPropName);

				if (destProp != null && destProp.getWriteMethod() != null) {
					final Object srcValue = srcProp.getReadMethod().invoke(src);
					destProp.getWriteMethod().invoke(dest, srcValue);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, PropertyDescriptor> getPropertyDescriptorsAsMap(Class cls) {
		Map<String, PropertyDescriptor> result = new LinkedHashMap<>();
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(cls);
		for (PropertyDescriptor p : propertyDescriptors) {
			result.put(p.getName(), p);
		}
		return result;
	}
}
