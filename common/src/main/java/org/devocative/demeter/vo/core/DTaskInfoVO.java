package org.devocative.demeter.vo.core;

public class DTaskInfoVO {
	private String type;
	private String cronExpression;
	private String calendar;

	// ------------------------------

	public DTaskInfoVO(String type, String cronExpression, String calendar) {
		this.type = type;
		this.cronExpression = cronExpression;
		this.calendar = calendar;
	}

	// ------------------------------

	public String getType() {
		return type;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public String getCalendar() {
		return calendar;
	}
}
