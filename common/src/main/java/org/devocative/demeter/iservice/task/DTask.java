package org.devocative.demeter.iservice.task;

import org.devocative.demeter.DSystemException;
import org.devocative.demeter.entity.DTaskState;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public abstract class DTask implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DTask.class);

	private Object id;
	private Object inputData;
	private Date startDate;

	private Long duration;
	private Exception exception;
	private DTaskState state = DTaskState.InQueue;
	private ITaskResultCallback resultCallback;
	private UserVO currentUser;

	// ------------------------------ ABSTRACT METHODS

	public abstract void init() throws Exception;

	public abstract boolean canStart() throws Exception;

	public abstract void execute() throws Exception;

	public abstract void cancel() throws Exception;

	// ------------------------------ ACCESSORS

	public Object getId() {
		return id;
	}

	public DTask setId(Object id) {
		this.id = id;
		return this;
	}

	public Object getInputData() {
		return inputData;
	}

	public DTask setInputData(Object inputData) {
		this.inputData = inputData;
		return this;
	}

	public DTask setResultCallback(ITaskResultCallback resultCallback) {
		this.resultCallback = resultCallback;
		return this;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Long getDuration() {
		return duration;
	}

	public DTaskState getState() {
		return state;
	}

	public Exception getException() {
		return exception;
	}

	public String getKey() {
		String key = getClass().getName();
		if (id != null) {
			key += "_" + id;
		}
		return key;
	}

	public UserVO getCurrentUser() {
		return currentUser;
	}

	public DTask setCurrentUser(UserVO currentUser) {
		this.currentUser = currentUser;
		return this;
	}

	// ------------------------------ PUBLIC

	@Override
	public final void run() {
		long start = System.currentTimeMillis();
		String key;
		if (currentUser != null && currentUser.getUsername() != null) {
			key = String.format("Task:%s:%s:%s", getClass().getSimpleName(), currentUser.getUsername(), id);
		} else {
			key = String.format("Task:%s:%s", getClass().getSimpleName(), id);
		}
		Thread.currentThread().setName(key);
		logger.info("Executing DTask: key=[{}]", key);

		try {
			init();
			if (canStart()) {
				startDate = new Date();
				state = DTaskState.Running;
				execute();
				state = DTaskState.Finished;
			} else {
				state = DTaskState.Invalid;
			}
		} catch (Exception e) {
			logger.error("DTask error: " + key, e);
			state = DTaskState.Error;
			exception = e;
			sendError(e);
		}

		duration = System.currentTimeMillis() - start;

		logger.info("Executed DTask: key=[{}] state=[{}] dur=[{}]", key, state, duration);
	}

	public final void stop() {
		state = DTaskState.Interrupted;
		try {
			cancel();
		} catch (Exception e) {
			logger.error("DTask: id={}", getId(), e);
			throw new RuntimeException(e);
		}
	}

	// ------------------------------ PROTECTED

	protected void sendResult(Object result) {
		if (resultCallback != null) {
			try {
				resultCallback.onTaskResult(id, result);
			} catch (Exception e) {
				logger.error("DTask.sendResult", e);
				sendError(e);
			}
		} else {
			throw new DSystemException("No ITaskResultCallback for DTask: " + getClass().getName());
		}
	}

	protected void sendError(Exception exception) {
		if (resultCallback != null) {
			try {
				resultCallback.onTaskError(id, exception);
			} catch (Exception e) {
				logger.error("DTask.sendError: ", e);
			}
		} else {
			throw new DSystemException("No ITaskResultCallback for DTask: " + getClass().getName());
		}
	}
}
