package org.devocative.demeter.iservice;

import java.util.concurrent.Future;

public interface ITaskService {
	Future<?> start(Class<? extends DTask> taskClass);

	Future<?> start(Class<? extends DTask> taskClass, String id);

	void stop(String key);

	void stopAll();
}
