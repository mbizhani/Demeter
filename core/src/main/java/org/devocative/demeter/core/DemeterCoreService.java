package org.devocative.demeter.core;

import org.devocative.demeter.core.xml.XDPage;
import org.devocative.demeter.core.xml.XDTask;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.iservice.IDemeterCoreService;
import org.devocative.demeter.vo.core.DModuleInfoVO;
import org.devocative.demeter.vo.core.DPageInfoVO;
import org.devocative.demeter.vo.core.DTaskInfoVO;

import java.util.*;

public class DemeterCoreService implements IDemeterCoreService {
	private List<DModuleInfoVO> modules;

	@Override
	public List<DModuleInfoVO> getModules() {
		if (modules == null) {
			modules = new ArrayList<>();

			Collection<XModule> xModules = DemeterCore.get().getModules().values();
			for (XModule xModule : xModules) {
				List<DPageInfoVO> pages = new ArrayList<>();
				if (xModule.getDPages() != null) {
					for (XDPage xdPage : xModule.getDPages()) {
						pages.add(new DPageInfoVO(xdPage.getType(), xdPage.getTitle(), xdPage.getUri(), xdPage.getInMenu(), xdPage.getRoles()));
					}
				}

				List<DTaskInfoVO> tasks = new ArrayList<>();
				if (xModule.getTasks() != null) {
					for (XDTask xdTask : xModule.getTasks()) {
						tasks.add(new DTaskInfoVO(xdTask.getType(), xdTask.getCronExpression(), xdTask.getCalendar()));
					}
				}

				modules.add(new DModuleInfoVO(xModule.getShortName(), xModule.getPrivilegeKeyClass(), pages, tasks));
			}
		}

		return modules;
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> var1) {
		return DemeterCore.get().getApplicationContext().getBeansOfType(var1);
	}

	@Override
	public <T> T getBean(Class<T> var1) {
		return DemeterCore.get().getApplicationContext().getBean(var1);
	}

	@Override
	public Object getBean(String var1) {
		return DemeterCore.get().getApplicationContext().getBean(var1);
	}

	@Override
	public Date getStartUpDate() {
		return DemeterCore.get().getStartUpDate();
	}
}
