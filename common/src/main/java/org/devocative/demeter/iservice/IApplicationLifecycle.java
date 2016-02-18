package org.devocative.demeter.iservice;

public interface IApplicationLifecycle {
	void init();

	void shutdown();

	ApplicationLifecyclePriority getLifecyclePriority();
}
