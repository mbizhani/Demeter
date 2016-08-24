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
		alert(errorThrown);
	});

	if(pingServerInterval) {
		Wicket.Event.subscribe("/websocket/open", function (jqEvent, message) {
			console.log('Demeter Wicket.Event: websocket/open!, ' + message);
			webSocketPingHandler = setInterval(pingServerByWS, pingServerInterval);
		});

		Wicket.Event.subscribe("/websocket/closed", function (jqEvent, message) {
			console.log('Demeter Wicket.Event: websocket/closed!, ' + message);
			window.clearInterval(webSocketPingHandler);
		});

		Wicket.Event.subscribe("/websocket/error", function (jqEvent, message) {
			console.log('Demeter Wicket.Event: websocket/error!, ' + message);
			window.clearInterval(webSocketPingHandler);
		});
	}

	if (sessionTO && sessionTO > 3000) {
		// alert user 3 sec before session timeout
		setInterval(onBeforeSessionTimeout, sessionTO - 3000);
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