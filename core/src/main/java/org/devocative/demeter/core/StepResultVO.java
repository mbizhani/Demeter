package org.devocative.demeter.core;

import java.io.Serializable;

public class StepResultVO implements Serializable {
	private static final long serialVersionUID = 4320860876760332286L;

	private EStartupStep step;
	private Exception error;

	// ------------------------------

	public StepResultVO(EStartupStep step) {
		this(step, null);
	}

	// Main Constructor
	public StepResultVO(EStartupStep step, Exception error) {
		this.step = step;
		this.error = error;
	}

	// ------------------------------

	public EStartupStep getStep() {
		return step;
	}

	public Exception getError() {
		return error;
	}

	// ---------------

	public boolean isSuccessful() {
		return error == null;
	}

}
