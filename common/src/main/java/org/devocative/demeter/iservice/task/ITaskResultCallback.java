package org.devocative.demeter.iservice.task;

public interface ITaskResultCallback {
	void onTaskResult(Object id, Object result);

	void onTaskError(Object id, Exception e);
}
