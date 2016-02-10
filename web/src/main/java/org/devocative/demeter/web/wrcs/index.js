window.onload = setupFunc;
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
}
function hideBusySign() {
	$("#ajaxVeil").css("display", "none");
}
function showBusySign() {
	$("#ajaxVeil").css("display", "inline");
}