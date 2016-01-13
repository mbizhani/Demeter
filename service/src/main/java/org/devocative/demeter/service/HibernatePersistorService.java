package org.devocative.demeter.service;

import org.devocative.demeter.iservice.IPersistorService;

import java.util.List;

public class HibernatePersistorService implements IPersistorService {
	@Override
	public void init(List<Class> entities, String driverClassName, String url, String username, String password) {

	}

	@Override
	public void shutdown() {

	}
}
