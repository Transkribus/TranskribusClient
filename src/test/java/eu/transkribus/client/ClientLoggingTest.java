package eu.transkribus.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientLoggingTest {
	private final static Logger logger = LoggerFactory.getLogger(ClientLoggingTest.class);

	public ClientLoggingTest() {
	}
	
	public static void main(String[] args) {
		System.out.println("start logging test, class = "+ClientLoggingTest.class.getName());
		
		logger.info("info!");
		logger.debug("debug!");
		logger.trace("trace!");
		
		logger.error("error!");
		logger.warn("warn!");
		// logger.fatal("fatal!");
	}

}
