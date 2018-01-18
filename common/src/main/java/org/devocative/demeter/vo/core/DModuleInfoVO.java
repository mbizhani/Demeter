package org.devocative.demeter.vo.core;

import java.util.List;

public class DModuleInfoVO {
	private String shortName;
	private String privilegeKeyClass;
	private List<DPageInfoVO> dPages;
	private List<DTaskInfoVO> tasks;

	// ------------------------------

	public DModuleInfoVO(String shortName, String privilegeKeyClass, List<DPageInfoVO> dPages, List<DTaskInfoVO> tasks) {
		this.shortName = shortName;
		this.privilegeKeyClass = privilegeKeyClass;
		this.dPages = dPages;
		this.tasks = tasks;
	}

	// ------------------------------

	public String getShortName() {
		return shortName;
	}

	public String getPrivilegeKeyClass() {
		return privilegeKeyClass;
	}

	public List<DPageInfoVO> getDPages() {
		return dPages;
	}

	public List<DTaskInfoVO> getTasks() {
		return tasks;
	}
}
