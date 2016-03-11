package org.devocative.demeter.iservice.task;

import java.util.concurrent.Future;

public interface ITaskService {
	Future<?> start(Class<? extends DTask> taskClass);

	Future<?> start(Class<? extends DTask> taskClass, String id);

	Future<?> start(Class<? extends DTask> taskClass, String id, Object inputData, ITaskResultCallback resultCallback);

	void stop(String key);

	void stopAll();
}
