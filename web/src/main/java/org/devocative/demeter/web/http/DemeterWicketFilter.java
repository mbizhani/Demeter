package org.devocative.demeter.web.http;

import org.apache.wicket.protocol.ws.javax.JavaxWebSocketFilter;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(
	filterName = "DemeterWicketFilter",
	urlPatterns = "/*",
	initParams = {
		@WebInitParam(
			name = "applicationClassName",
			value = "org.devocative.demeter.web.DemeterWebApplication")})
public class DemeterWicketFilter extends JavaxWebSocketFilter {
}
