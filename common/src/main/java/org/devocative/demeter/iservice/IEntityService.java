package org.devocative.demeter.iservice;

public interface IEntityService<T> {
	T load(Long id);
}
