package eu.transkribus.client.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Cookie;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn;

public class ClientRequestAuthFilter2 implements ClientRequestFilter {
	private final static Logger logger = LoggerFactory.getLogger(ClientRequestAuthFilter.class);
	private final static String COOKIE_NAME = "JSESSIONID";
	
	ATrpServerConn conn;
	
	public ClientRequestAuthFilter2(ATrpServerConn conn) {
		Assert.assertNotNull(conn);
		
		this.conn = conn;
	}
	
	private List<Object> getCookies() {
		if (conn.getUserLogin() == null) {
			logger.trace("no user login - setting no cookies");
			return null;
		}
		if (conn.getUserLogin().getSessionId() == null) {
			logger.trace("no sessionid in login - setting no cookies");	
			return null;
		}
		
		logger.trace("ClientRequestAuthFilter2. JSESSIONID=" + conn.getUserLogin().getSessionId());
		
		Cookie sessionCookie = new Cookie(COOKIE_NAME, conn.getUserLogin().getSessionId());
		List<Object> cookies = new ArrayList<>(1);
		cookies.add(sessionCookie);
		
		return cookies;
	}
	
	private List<Object> setCookieHeader(ClientRequestContext requestContext) {
		List<Object> cookies = getCookies();
		if(requestContext.getHeaders().get("Cookie") == null && cookies != null) {
			logger.trace("Putting cookies to requestContext.");
			requestContext.getHeaders().put("Cookie", cookies);
		}
		return cookies;
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		setCookieHeader(requestContext);
		
		if (!requestContext.getCookies().containsKey(COOKIE_NAME)){
			logger.trace("Session ID is not in cookies.");
		} else {
			String id = requestContext.getCookies().get(COOKIE_NAME).getValue();
			logger.trace("Session ID is in cookies: " + id);
		}
	}
}
