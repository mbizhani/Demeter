package org.devocative.demeter.iservice.task;

import org.devocative.demeter.entity.DTaskInfo;

import java.util.List;

public interface ITaskService {
	DTaskInfo load(Long id);

	DTaskInfo loadByType(String type);

	List<DTaskInfo> search(long pageIndex, long pageSize);

	long count();

	// ==============================

	DTaskResult start(Long taskInfoId, Object id, Object inputData, ITaskResultCallback resultCallback);

	DTaskResult start(String taskBeanId, Object id, Object inputData, ITaskResultCallback resultCallback);

	void stop(String key);

	void stopAll();
}
