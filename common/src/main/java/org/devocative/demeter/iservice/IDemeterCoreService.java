package org.devocative.demeter.iservice;

import org.devocative.demeter.vo.core.DModuleInfoVO;

import java.util.List;
import java.util.Map;

public interface IDemeterCoreService {
	String JOB_KEY = "CORE_SERVICE";

	List<DModuleInfoVO> getModules();

	<T> Map<String, T> getBeansOfType(Class<T> var1);

	<T> T getBean(Class<T> var1);

	Object getBean(String var1);
}
