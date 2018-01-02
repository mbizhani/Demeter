package org.devocative.demeter.iservice.task;

public interface ITaskResultEvent {
	void onTaskResult(DTask dTask, Object result);

	void onTaskError(DTask dTask, Exception e);
}
