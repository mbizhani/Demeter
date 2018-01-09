package org.devocative.demeter.iservice.task;

public interface ITaskResultCallback<T> {
	void onTaskResult(Object id, T result);

	void onTaskError(Object id, Exception e);
}
