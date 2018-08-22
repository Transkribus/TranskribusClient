package eu.transkribus.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.connection.TrpServerConn;

public class ATrpClientTest {
	protected static final String TEST_CREDS_FILE_NAME = "testCreds.properties";
	
	protected static String username;
	protected static String password;
	protected static TrpServerConn client;
	
	/**
	 * Collection with data for JUnit tests on the test server
	 */
	protected static final int TEST_COLLECTION_ID = 1915;
	
	@BeforeClass
	public static void initClient() throws IOException, LoginException {
		Properties creds = new Properties();
		try (InputStream is = ATrpClientTest.class.getClassLoader().getResourceAsStream(TEST_CREDS_FILE_NAME)) {
			if(is == null) {
				URL url = ATrpClientTest.class.getClassLoader().getResource(TEST_CREDS_FILE_NAME);
				throw new RuntimeException("Could not find credentials file for test user at: " + url);
			}
			creds.load(is);
		}
		username = creds.getProperty("username");
		password = creds.getProperty("password");
		client = new TrpServerConn(TrpServer.Test, username, password);		
		client.enableDebugLogging(true);
	}
	
	@Test
	public void checkSession() throws SessionExpiredException, ClientErrorException, ServerErrorException {
		client.checkSession();
	}
	
	@AfterClass
	public static void shutdownClient() {
		if(client != null) {
			client.logout();
		}
	}
}
