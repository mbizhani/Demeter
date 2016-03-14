package org.devocative.demeter.iservice.task;

import java.util.concurrent.Future;

public interface ITaskService {
	Future<?> start(String taskBeanId);

	Future<?> start(String taskBeanId, String id);

	Future<?> start(String taskBeanId, String id, Object inputData, ITaskResultCallback resultCallback);

	Future<?> start(Class<? extends DTask> taskClass, String id);

	void stop(String key);

	void stopAll();
}
