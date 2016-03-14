package org.devocative.demeter.web;

import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.wickomp.async.AsyncMediator;
import org.devocative.wickomp.async.AsyncToken;
import org.devocative.wickomp.async.IAsyncRequestHandler;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WebDModule implements ITaskResultCallback {
	private final Map<String, AsyncToken> TOKENS_MAP = new ConcurrentHashMap<>();

	public abstract void init();

	protected void storeAsyncToken(AsyncToken token) {
		TOKENS_MAP.put(token.getId(), token);
	}

	protected AsyncToken getAndRemove(String id) {
		return TOKENS_MAP.remove(id);
	}

	protected void registerAsyncHandler(String handlerId, IAsyncRequestHandler asyncHandler) {
		AsyncMediator.registerHandler(handlerId, asyncHandler);
	}

	protected void pushResponseToPage(AsyncToken asyncToken, Serializable responsePayLoad) {
		AsyncMediator.sendResponse(asyncToken, responsePayLoad);
	}

	protected void pushBroadcastToPages(Object message) {
		AsyncMediator.broadcast(message);
	}
}
