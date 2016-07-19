package org.devocative.demeter.iservice.task;

import org.devocative.demeter.DSystemException;
import org.devocative.demeter.entity.DTaskState;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DTask implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DTask.class);

	private String id;
	private Object inputData;
	private Date startDate;

	private Long duration;
	private Exception exception;
	private DTaskState state = DTaskState.InQueue;
	private ITaskResultCallback resultCallback;
	private UserVO currentUser;

	private AtomicBoolean continue_ = new AtomicBoolean(true);

	// ------------------------------ ABSTRACT METHODS

	public abstract void init();

	public abstract boolean canStart();

	public abstract void execute();

	// ------------------------------ ACCESSORS

	public String getId() {
		return id;
	}

	public DTask setId(String id) {
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
	public void run() {
		long start = System.currentTimeMillis();
		String key;
		if (currentUser != null && currentUser.getUsername() != null) {
			key = String.format("Task:%s:%s:%s", getClass().getSimpleName(), currentUser.getUsername(), id);
		} else {
			key = String.format("Task:%s:%s", getClass().getSimpleName(), id);
		}
		Thread.currentThread().setName(key);
		logger.info("Executing DTask: key=[{}]", key);

		init();
		if (canStart()) {
			startDate = new Date();
			state = DTaskState.Running;
			try {
				execute();
				state = DTaskState.Finished;
			} catch (Exception e) {
				logger.error("DTask error: " + key, e);
				state = DTaskState.Error;
				exception = e;
				if (resultCallback != null) {
					resultCallback.onTaskError(id, e);
				}
			}
		} else {
			state = DTaskState.Invalid;
		}

		duration = System.currentTimeMillis() - start;

		logger.info("Executed DTask: key=[{}] state=[{}] dur=[{}]", key, state, duration);
	}

	public final void stop() {
		continue_.set(false);
		state = DTaskState.Interrupted;
	}

	// ------------------------------ PROTECTED

	protected boolean canContinue() {
		return continue_.get();
	}

	protected void setResult(Object result) {
		if (resultCallback != null) {
			resultCallback.onTaskResult(id, result);
		} else {
			throw new DSystemException("No ITaskResultCallback for DTask: " + getClass().getName());
		}
	}
}
