var webSocketPingHandler;
var idleSessionCounter = 0;
var confirmDlg = null;

wLog.info("Demeter Init: WebSocketPing=[" + WebSocketPingInterval + "] SessionTO=[" + sessionTO + "]");

hideBusySign();

Wicket.Event.subscribe("/ajax/call/beforeSend", function (attributes, jqXHR, settings) {
	showBusySign();
	idleSessionCounter = 0;
});
Wicket.Event.subscribe("/ajax/call/complete", function (attributes, jqXHR, textStatus) {
	hideBusySign();
});
Wicket.Event.subscribe("/ajax/call/failure", function (attributes, jqXHR, errorThrown, textStatus) {
	hideBusySign();
	$.messager.alert("Err", errorThrown);
});

Wicket.Event.subscribe("/websocket/open", function (jqEvent) {
	wLog.info("Demeter: websocket/open");

	if (WebSocketPingInterval) {
		webSocketPingHandler = setInterval(pingWebSocket, WebSocketPingInterval * 1000);
	}
});
Wicket.Event.subscribe("/websocket/closed", function (jqEvent) {
	wLog.warn("Demeter: websocket/closed", jqEvent);
	clearInterval(webSocketPingHandler);
});
Wicket.Event.subscribe("/websocket/error", function (jqEvent) {
	wLog.error("Demeter: websocket/error", jqEvent);
});
Wicket.Event.subscribe("/websocket/message", function (jqEvent, message) {
	if (message && message != "W.R_PING") {
		if (sessionTO && sessionTO > 0 && idleSessionCounter > (sessionTO - 35)) {
			wLog.info("Demeter: resend ajax on ws response, reset idle");
			Wicket.Ajax.get({u: ajaxUrl});
		}
	}
});

if (sessionTO && sessionTO > 0) {
	setInterval(processIdleSession, 1000);
}

function hideBusySign() {
	$("#ajaxVeil").css("display", "none");
}

function showBusySign() {
	$("#ajaxVeil").css("display", "inline");
}

function pingWebSocket() {
	Wicket.WebSocket.send("W.PING");
}

function processIdleSession() {
	idleSessionCounter++;

	if (idleSessionCounter > (sessionTO - 30) && confirmDlg == null) {
		wLog.warn("30sec Before Expiration!");

		confirmDlg = $.messager.confirm("", "Session is to expired. Reconnect?", function (r) {
			confirmDlg = null;
			if (r) {
				Wicket.Ajax.get({u: ajaxUrl});
			}
		});
	}
}