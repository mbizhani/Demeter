window.onload = setupFunc;
var webSocketPingHandler;
var sessionTOHandler;

function setupFunc() {
	hideBusySign();

	Wicket.Event.subscribe('/ajax/call/beforeSend', function (attributes, jqXHR, settings) {
		showBusySign();
	});
	Wicket.Event.subscribe('/ajax/call/complete', function (attributes, jqXHR, textStatus) {
		hideBusySign();
	});
	Wicket.Event.subscribe('/ajax/call/failure', function (attributes, jqXHR, errorThrown, textStatus) {
		hideBusySign();
		alert(errorThrown);
	});

	if(pingServerInterval) {
		Wicket.Event.subscribe("/websocket/open", function (jqEvent, message) {
			webSocketPingHandler = setInterval(pingServerByWS, pingServerInterval);
		});

		Wicket.Event.subscribe("/websocket/closed", function (jqEvent, message) {
			window.clearInterval(webSocketPingHandler);
		});

		Wicket.Event.subscribe("/websocket/error", function (jqEvent, message) {
			window.clearInterval(webSocketPingHandler);
		});
	}

	if(sessionTO && sessionTO > 0) {
		sessionTOHandler = setInterval(onBeforeSessionTimeout, sessionTO);
	} else if(sessionTOHandler) {
		window.clearInterval(webSocketPingHandler);
	}
}
function hideBusySign() {
	$("#ajaxVeil").css("display", "none");
}
function showBusySign() {
	$("#ajaxVeil").css("display", "inline");
}
function pingServerByWS() {
	Wicket.WebSocket.send('{msg:"p"}');
}
function onBeforeSessionTimeout() {
	if(confirm('Session is to expire.Reconnect?')) {
		Wicket.Ajax.get({u:ajaxUrl});
	}
}