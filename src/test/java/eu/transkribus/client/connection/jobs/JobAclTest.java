package eu.transkribus.client.connection.jobs;

import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.ATrpClientTest;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.job.enums.JobImpl;

public class JobAclTest extends ATrpClientTest {
	private static final Logger logger = LoggerFactory.getLogger(JobAclTest.class);
	
	@Test
	public void getJobAclTest() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		List<String> acl = client.getJobAcl();
		logger.info("Test user is allowed for jobs:");
		for(String i : acl) {
			logger.info("" + i);
		}
	}
	
	@Test
	public void testEquivalenceOfEndpoints() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		for(JobImpl jobImpl : JobImpl.values()) {
			boolean newMethod = client.isUserAllowedForJob(""+jobImpl);
			boolean oldMethod;
			try {
				oldMethod = client.isUserAllowedForJobOld(""+jobImpl);
			} catch(Exception e) {
				oldMethod = false;
			}
			Assert.assertTrue("Equivalence check failed for job: " + jobImpl, newMethod == oldMethod);
		}
	}

}
