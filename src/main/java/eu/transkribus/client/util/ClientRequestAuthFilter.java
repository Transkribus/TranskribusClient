package eu.transkribus.client.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn;

public class ClientRequestAuthFilter implements ClientRequestFilter {
	private final static Logger logger = LoggerFactory.getLogger(ClientRequestAuthFilter.class);
	private final static String COOKIE_NAME = "JSESSIONID";  
	private static List<Object> cookies = null;

	public ClientRequestAuthFilter(final String SESSION_ID) {
		logger.debug("Initializing ClientRequestAuthFilter. JSESSIONID=" + SESSION_ID);
		Cookie sessionCookie = new Cookie(COOKIE_NAME, SESSION_ID);
		cookies = new ArrayList<>(1);
		cookies.add(sessionCookie);
	}	

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		if(requestContext.getHeaders().get("Cookie") == null){
//			logger.debug("Putting cookies to requestContext.");
			requestContext.getHeaders().put("Cookie", cookies);
		}
		
		logger.debug("Cookies = " + cookies.toString());
		if (!requestContext.getCookies().containsKey(COOKIE_NAME)){
//			logger.debug("Session ID is not in cookies.");
		} else {
			String id = requestContext.getCookies().get(COOKIE_NAME).getValue();
//			logger.debug("Session ID is in cookies: " + id);
		}
	}
}
