package org.devocative.demeter.web;

import org.apache.wicket.protocol.https.HttpsConfig;
import org.apache.wicket.protocol.https.HttpsMapper;
import org.apache.wicket.protocol.https.Scheme;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class DemeterHttpsMapper extends HttpsMapper {
	private static final Logger logger = LoggerFactory.getLogger(DemeterHttpsMapper.class);

	// ------------------------------

	public DemeterHttpsMapper(IRequestMapper delegate, HttpsConfig config) {
		super(delegate, config);
	}

	// ------------------------------

	@Override
	protected Scheme getSchemeOf(Request request) {
		HttpServletRequest req = (HttpServletRequest) request.getContainerRequest();

		if ("https".equalsIgnoreCase(req.getScheme())) {
			return Scheme.HTTPS;
		} else if ("http".equalsIgnoreCase(req.getScheme())) {
			return Scheme.HTTP;
		} else if ("ws".equalsIgnoreCase(req.getScheme()) || "wss".equalsIgnoreCase(req.getScheme())) {
			logger.debug("WebSocketSchema: URL={}", req.getRequestURL());
			return Scheme.ANY;
		} else {
			logger.error("Unknown request schema: schema={}, URL={}", req.getScheme(), req.getRequestURL());
			throw new IllegalStateException(String.format("Could not resolve protocol for request: schema=[%s], url=[%s]",
				req.getScheme(), req.getRequestURI()));
		}
	}

}
