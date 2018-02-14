package org.devocative.demeter.iservice.persistor;

import org.devocative.demeter.iservice.IApplicationLifecycle;
import org.devocative.demeter.iservice.IRequestLifecycle;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IPersistorService extends IApplicationLifecycle, IRequestLifecycle {
	void setInitData(List<Class> entities, String prefix);

	void commitOrRollback();

	void rollback();

	void endSession();

	void saveOrUpdate(Object obj);

	Serializable save(Object obj);

	void update(Object obj);

	Object updateFields(Object obj, String... fields);

	void persist(Object obj);

	<T> T merge(T obj);

	<T> T get(Class<T> entity, Serializable id);

	<T> T load(Class<T> entity, Serializable id, ELockMode lockMode);

	void delete(Class entity, Serializable id);

	void delete(Object obj);

	<T> List<T> list(Class<T> entity);

	<T> List<T> list(String simpleQuery);

	void refresh(Object entity);

	void executeUpdate(String simpleQuery);

	IQueryBuilder createQueryBuilder();

	void generateSchemaDiff();

	void executeScript(String script, String delimiter);

	Connection createSqlConnection() throws SQLException;
}
