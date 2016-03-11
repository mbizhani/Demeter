package org.devocative.demeter.iservice.task;

public interface ITaskResultCallback {
	void setResult(Object result);

	void setException(Exception e);
}
