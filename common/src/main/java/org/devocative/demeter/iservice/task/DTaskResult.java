package org.devocative.demeter.iservice.task;

import java.util.concurrent.Future;

public class DTaskResult {
	private Future<?> future;
	private DTask taskInstance;

	// ------------------------------

	public DTaskResult(Future<?> future, DTask taskInstance) {
		this.future = future;
		this.taskInstance = taskInstance;
	}

	// ------------------------------

	public Future<?> getFuture() {
		return future;
	}

	public DTask getTaskInstance() {
		return taskInstance;
	}
}
