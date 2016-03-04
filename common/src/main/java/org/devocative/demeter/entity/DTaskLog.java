package org.devocative.demeter.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_dmt_d_task_log")
public class DTaskLog implements Serializable {
	@Id
	@GeneratedValue(generator = "dmt_d_task_log")
	@org.hibernate.annotations.GenericGenerator(name = "dmt_d_task_log", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "dmt_d_task_log")
		})
	private Long id;

	@Column(name = "c_key", nullable = false)
	private String key;

	@Column(name = "d_start", nullable = false, columnDefinition = "date")
	private Date start;

	@Column(name = "d_end", columnDefinition = "date")
	private Date end;

	@Column(name = "c_state", nullable = false)
	private DTaskState state;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_task", foreignKey = @ForeignKey(name = "tasklog2task"))
	private DTaskInfo task;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public DTaskState getState() {
		return state;
	}

	public void setState(DTaskState state) {
		this.state = state;
	}

	public DTaskInfo getTask() {
		return task;
	}

	public void setTask(DTaskInfo task) {
		this.task = task;
	}
}
