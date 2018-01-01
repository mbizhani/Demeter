package org.devocative.demeter.iservice.task;

import org.devocative.demeter.entity.DTaskInfo;
import org.devocative.demeter.vo.DTaskVO;
import org.devocative.demeter.vo.filter.DTaskFVO;

import java.util.List;

public interface ITaskService {
	DTaskInfo load(Long id);

	DTaskInfo loadByType(String type);

	List<DTaskInfo> search(long pageIndex, long pageSize);

	long count();

	// ==============================

	DTaskResult start(Class<? extends DTask> taskBeanClass, Object inputData, ITaskResultCallback resultCallback);

	DTaskResult start(Class<? extends DTask> taskBeanClass, Object id, Object inputData, ITaskResultCallback resultCallback);

	DTaskResult start(Long taskInfoId, Object id, Object inputData, ITaskResultCallback resultCallback);

	void stop(Class<? extends DTask> taskBeanClass, Object id);

	void stop(String key);

	void stopAll();

	List<DTaskVO> search(DTaskFVO dTaskFVO, long pageIndex, long pageSize);

	long count(DTaskFVO dTaskFVO);
}
