package eu.transkribus.client.util;

import org.slf4j.Logger;


/**
 * Jersey 2.7 only allows to use java.util.logging in its LoggingFilter. This facade can be used as a bridge to slf4j.<br/>
 * LoggingFilter is deprecated in new versions of Jersey. On update, all instances have to be replaced by LoggingFeature and this may not longer work.
 * 
 * @author philip
 *
 */
public class JulFacade extends java.util.logging.Logger {
	private final Logger logger;
	
	public JulFacade(final Logger logger) {
		super("Jersey", null);
		this.logger = logger;
	}

	@Override
	public void info(String msg) {
		logger.info(msg);
	}
}
