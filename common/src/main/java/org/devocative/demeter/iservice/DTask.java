package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.DTaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DTask implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DTask.class);
	private static final ThreadLocal<String> KEY = new ThreadLocal<String>();

	private String id;
	private Date startDate;
	private Long duration;
	private Exception exception;
	private DTaskState state = DTaskState.InQueue;

	private AtomicBoolean continue_ = new AtomicBoolean(true);

	// -------------- main abstract methods

	public abstract void init();

	public abstract boolean canStart();

	public abstract void execute();

	// -------------- Accessors

	public String getId() {
		return id;
	}

	public DTask setId(String id) {
		this.id = id;
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

	public static String getKEY() {
		return KEY.get();
	}

	// --------------

	@Override
	public void run() {
		KEY.set(getKey());

		long start = System.currentTimeMillis();
		String key = String.format("Task:%s{%s}", getClass().getSimpleName(), id);
		Thread.currentThread().setName(key);
		logger.info("Running DTask: {}", key);

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
			}
		} else {
			state = DTaskState.Invalid;
		}

		duration = System.currentTimeMillis() - start;
	}

	public final void stop() {
		continue_.set(false);
		state = DTaskState.Interrupted;
	}

	// --------------

	protected boolean canContinue() {
		return continue_.get();
	}
}
