package org.devocative.demeter.web;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.wickomp.WebUtil;
import org.devocative.wickomp.async.IAsyncResponse;
import org.devocative.wickomp.async.WebSocketDelayedResponse;
import org.devocative.wickomp.async.WebSocketToken;

public class DTaskBehavior extends Behavior implements ITaskResultCallback {
	private static final long serialVersionUID = 3648272313697986956L;

	private WebSocketToken token;
	private IAsyncResponse response;

	// ------------------------------

	public DTaskBehavior(IAsyncResponse response) {
		this.response = response;
	}

	// ------------------------------

	@Override
	public void beforeRender(Component component) {
		if (token == null) {
			token = WebUtil.createWSToken(component);
		}
	}

	// --------------- ITaskResultCallback

	@Override
	public void onTaskResult(Object id, Object result) {
		WebUtil.wsPush(token, new WebSocketDelayedResponse(response, result));
	}

	@Override
	public void onTaskError(Object id, Exception e) {
		WebUtil.wsPush(token, new WebSocketDelayedResponse(response, e));
	}
}
