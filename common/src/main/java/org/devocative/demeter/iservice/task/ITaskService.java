package org.devocative.demeter.iservice.task;

import org.devocative.demeter.entity.DTaskInfo;

import java.util.List;
import java.util.concurrent.Future;

public interface ITaskService {
	DTaskInfo load(Long id);

	DTaskInfo loadByType(String type);

	Future<?> start(Long taskInfoId, String id, Object inputData, ITaskResultCallback resultCallback);

	List<DTaskInfo> search(long pageIndex, long pageSize);

	long count();

	Future<?> start(String taskBeanId, String id, Object inputData, ITaskResultCallback resultCallback);

	void stop(String key);

	void stopAll();
}
