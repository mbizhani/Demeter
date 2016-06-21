package org.devocative.demeter.iservice.task;

public interface ITaskResultCallback {
	void onTaskResult(String id, Object result);

	void onTaskError(String id, Exception e);
}
