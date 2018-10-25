package eu.transkribus.client.connection.jobs;

import java.util.Arrays;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.ATrpClientTest;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.job.TrpJobStatus;

public class TableMatchingTest extends ATrpClientTest {
	private static final Logger logger = LoggerFactory.getLogger(TableMatchingTest.class);
	
//	@Test
	public void tableMatchingTest() throws SessionExpiredException, ClientErrorException, ServerErrorException {
		final int docId = 4263;
		final int templateTsId = 35165;
		DocumentSelectionDescriptor dsd = new DocumentSelectionDescriptor(docId);
		TrpJobStatus job = client.analyzeTables(TEST_COLLECTION_ID, Arrays.asList(dsd), templateTsId);
		logger.info("Started table matching job: " + job);
		job = super.waitForJobToEnd(job);
		Assert.assertEquals("Job did not succeed!", TrpJobStatus.FINISHED, job.getState());
	}
}
