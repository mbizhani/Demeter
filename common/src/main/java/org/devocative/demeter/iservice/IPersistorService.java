package org.devocative.demeter.iservice;

import java.io.Serializable;
import java.util.List;

public interface IPersistorService {
	void init(List<Class> entities, String prefix);

	void shutdown();

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
}
