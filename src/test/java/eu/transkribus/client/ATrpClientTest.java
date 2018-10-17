package eu.transkribus.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor.PageDescriptor;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.client.connection.TrpServerConn;

public class ATrpClientTest {
	private static final Logger logger = LoggerFactory.getLogger(ATrpClientTest.class);
	
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

	public TrpJobStatus waitForJobToEnd(TrpJobStatus job) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException {
		while(!(job.isFailed() || job.isFinished())) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				logger.error("Interruption while waiting for Job to finish!", e);
			}
			job = client.getJob(job.getJobId());
			logger.info("Job state = " + job.getState());
		}
		return job;
	}
	
	public List<DocumentSelectionDescriptor> getDds(int collId) throws Exception {
		List<TrpDocMetadata> docMds = client.getAllDocs(collId, 0, 0, null, null, false);
		System.out.println("n-docs = "+docMds.size());
		List<DocumentSelectionDescriptor> dds = new ArrayList<>();
		
		for (TrpDocMetadata dm : docMds) {
			TrpDoc d = client.getTrpDoc(collId, dm.getDocId(), 1);
			
			DocumentSelectionDescriptor dd = new DocumentSelectionDescriptor();
			dd.setDocId(d.getId());
			
			for (TrpPage p : d.getPages()) {
				PageDescriptor pd = new PageDescriptor();
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
				
				pd.setPageId(p.getPageId());
				pd.setTsId(tmd.getTsId());
				
				dd.getPages().add(pd);
			}
			
			dds.add(dd);
		}
		
		return dds;
	}
}
