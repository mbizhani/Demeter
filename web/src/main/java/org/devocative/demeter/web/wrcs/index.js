window.onload = setupFunc;
var webSocketPingHandler;

function setupFunc() {
	console.log('Demeter SetupFunc');

	hideBusySign();

	Wicket.Event.subscribe('/ajax/call/beforeSend', function (attributes, jqXHR, settings) {
		showBusySign();
	});
	Wicket.Event.subscribe('/ajax/call/complete', function (attributes, jqXHR, textStatus) {
		hideBusySign();
	});
	Wicket.Event.subscribe('/ajax/call/failure', function (attributes, jqXHR, errorThrown, textStatus) {
		hideBusySign();
		$.messager.alert('', errorThrown);
	});

	try {
		if (WebSocketPingInterval) {
			Wicket.Event.subscribe("/websocket/open", function (jqEvent, message) {
				console.log('Demeter: websocket/open!, ', message);
				webSocketPingHandler = setInterval(pingWebSocket, WebSocketPingInterval);
			});

			Wicket.Event.subscribe("/websocket/closed", function (jqEvent, message) {
				console.log('Demeter: websocket/closed!, ', message);
				window.clearInterval(webSocketPingHandler);
			});

			Wicket.Event.subscribe("/websocket/error", function (jqEvent, message) {
				console.log('Demeter: websocket/error!, ', message);
				window.clearInterval(webSocketPingHandler);
			});
		}

		if (sessionTO && sessionTO > 3000) {
			// alert user 3 sec before session timeout
			setInterval(onBeforeSessionTimeout, sessionTO - 3000);
		}
	} catch (e) {
		console.log("Undefined init var: " + e);
	}
}

function hideBusySign() {
	$("#ajaxVeil").css("display", "none");
}

function showBusySign() {
	$("#ajaxVeil").css("display", "inline");
}

function pingWebSocket() {
	Wicket.WebSocket.send('PING');
}

function onBeforeSessionTimeout() {
	$.messager.confirm('', 'Session is to expired. Reconnect?', function (r) {
		if (r) {
			Wicket.Ajax.get({u: ajaxUrl});
		}
	});
}