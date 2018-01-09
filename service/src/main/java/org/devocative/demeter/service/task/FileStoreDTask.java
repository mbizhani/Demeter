package org.devocative.demeter.service.task;

import org.devocative.demeter.iservice.IFileStoreService;
import org.devocative.demeter.iservice.task.DTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("dmtFileStoreDTask")
public class FileStoreDTask extends DTask<Void> {

	@Autowired
	private IFileStoreService fileStoreService;

	@Override
	public void init() {
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() {
		fileStoreService.doExpire();
	}

	@Override
	public void cancel() throws Exception {
		throw new RuntimeException("Not Implemented!");
	}
}
