package org.devocative.demeter.iservice.task;

import org.devocative.demeter.entity.DTaskState;
import org.devocative.demeter.vo.RequestVO;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class DTask<T> implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DTask.class);

	private Object id;
	private Object inputData;
	private Date startDate;

	private Long duration;
	private Exception exception;
	private DTaskState state = DTaskState.InQueue;
	private ITaskResultEvent taskResultEvent;

	private UserVO currentUser;
	private RequestVO currentRequest;

	private List<ITaskResultCallback> resultCallbacks = new ArrayList<>();

	private String detail;

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

	public DTask setTaskResultEvent(ITaskResultEvent taskResultEvent) {
		this.taskResultEvent = taskResultEvent;
		return this;
	}

	public DTask addTaskResultCallback(ITaskResultCallback resultCallback) {
		if (resultCallback != null) {
			resultCallbacks.add(resultCallback);
		}
		return this;
	}

	public List<ITaskResultCallback> getResultCallbacks() {
		return resultCallbacks;
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

	public RequestVO getCurrentRequest() {
		return currentRequest;
	}

	public DTask<T> setCurrentRequest(RequestVO currentRequest) {
		this.currentRequest = currentRequest;
		return this;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
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
			if (state == DTaskState.InQueue) {
				init();
				if (canStart()) {
					startDate = new Date();
					state = DTaskState.Running;
					execute();
					if (state == DTaskState.Running) {
						state = DTaskState.Finished;
					}
				} else {
					state = DTaskState.Invalid;
				}
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

	public final void stop() throws Exception {
		cancel();
		state = DTaskState.Interrupted;
	}

	// ------------------------------ PROTECTED

	protected void sendResult(T result) {
		try {
			taskResultEvent.onTaskResult(this, result);
		} catch (Exception e) {
			logger.error("DTask.sendResult", e);
			sendError(e);
		}
	}

	protected void sendError(Exception exception) {
		try {
			taskResultEvent.onTaskError(this, exception);
		} catch (Exception e) {
			logger.error("DTask.sendError: ", e);
		}
	}
}
