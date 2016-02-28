package org.devocative.demeter.iservice.persistor;

import org.devocative.demeter.iservice.IApplicationLifecycle;
import org.devocative.demeter.iservice.IRequestLifecycle;

import java.io.Serializable;
import java.util.List;

public interface IPersistorService extends IApplicationLifecycle, IRequestLifecycle {
	void setInitData(List<Class> entities, String prefix);

	void commitOrRollback();

	void rollback();

	void endSession();

	void saveOrUpdate(Object obj);

	void save(Object obj);

	void update(Object obj);

	void merge(Object obj);

	<T> T get(Class<T> entity, Serializable id);

	<T> T load(Class<T> entity, Serializable id, ELockMode lockMode);

	void delete(Class entity, Serializable id);

	void delete(Object obj);

	<T> List<T> list(Class<T> entity);

	<T> List<T> list(String simpleQuery);

	void refresh(Object entity);

	void executeUpdate(String simpleQuery);

	IQueryBuilder createQueryBuilder();
}
