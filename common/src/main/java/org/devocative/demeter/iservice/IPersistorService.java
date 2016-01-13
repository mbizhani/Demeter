package org.devocative.demeter.iservice;

import java.util.List;

public interface IPersistorService {
	void init(List<Class> entities, String driverClassName, String url, String username, String password);

	void shutdown();

}
