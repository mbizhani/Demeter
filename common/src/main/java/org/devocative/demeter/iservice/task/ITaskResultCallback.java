package org.devocative.demeter.iservice.task;

public interface ITaskResultCallback {
	void onTaskResult(Object token, Object result);

	void onTaskError(Object token, Exception e);
}
