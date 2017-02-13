package org.devocative.demeter.web;

import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.wickomp.async.AsyncMediator;
import org.devocative.wickomp.async.AsyncToken;
import org.devocative.wickomp.async.IAsyncRequestHandler;

import java.io.Serializable;

public abstract class WebDModule implements ITaskResultCallback {
	public abstract void init();

	// ------------------------------

	@Override
	public void onTaskResult(Object token, Object result) {
		pushResponseToPage((AsyncToken) token, (Serializable) result);
	}

	@Override
	public void onTaskError(Object token, Exception e) {
		pushErrorToPage((AsyncToken) token, e);
	}

	// ------------------------------

	protected void registerAsyncHandler(String handlerId, IAsyncRequestHandler asyncHandler) {
		AsyncMediator.registerHandler(handlerId, asyncHandler);
	}

	protected void pushResponseToPage(AsyncToken asyncToken, Serializable responsePayLoad) {
		AsyncMediator.sendResponse(asyncToken, responsePayLoad);
	}

	protected void pushErrorToPage(AsyncToken asyncToken, Exception error) {
		AsyncMediator.sendError(asyncToken, error);
	}

	protected void pushBroadcastToPages(Object message) {
		AsyncMediator.broadcast(message);
	}
}
