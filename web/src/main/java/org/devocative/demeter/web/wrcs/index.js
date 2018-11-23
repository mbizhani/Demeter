var confirmDlg = null;
var wsPingHandler = null;
var idleSessionCounter = 0;

wLog.info("Demeter Init: WebSocketPing=[" + WebSocketPingInterval + "] SessionTO=[" + sessionTO + "]");

hideBusySign();

Wicket.Event.subscribe("/ajax/call/beforeSend", function (attributes, jqXHR, settings) {
	showBusySign();
	confirmDlg = null;
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

	Wicket.WebSocket.send("W::OPEN");

	if (WebSocketPingInterval) {
		wsPingHandler = setInterval(pingWebSocket, WebSocketPingInterval * 1000);
	}
});
Wicket.Event.subscribe("/websocket/closed", function (jqEvent) {
	wLog.warn("Demeter: websocket/closed", jqEvent);

	clearInterval(wsPingHandler);

	$("#wsDisconnected").css("display", "inline");
});
Wicket.Event.subscribe("/websocket/error", function (jqEvent) {
	wLog.error("Demeter: websocket/error", jqEvent);
});
Wicket.Event.subscribe("/websocket/message", function (jqEvent, message) {
	wLog.info("Demeter: websocket/message", message);
});

if (sessionTO > 0) {
	setInterval(processIdleSession, 1000);
}

function hideBusySign() {
	$("#ajaxVeil").css("display", "none");
}

function showBusySign() {
	$("#ajaxVeil").css("display", "inline");
}

function pingWebSocket() {
	Wicket.WebSocket.send("W::PING");
}

function processIdleSession() {
	idleSessionCounter++;

	if (idleSessionCounter > (sessionTO - 30) && confirmDlg == null) {
		wLog.warn("30sec Before Expiration!");

		confirmDlg = $.messager.confirm("", "Session is to expired. Reconnect?", function (r) {
			if (r) {
				Wicket.Ajax.get({u: ajaxUrl});
			}
		});
	}
}