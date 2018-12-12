package eu.transkribus.client.connection;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.ATrpClientTest;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpFImagestore;

public class FImagestoreConfigTest extends ATrpClientTest {
	private static final Logger logger = LoggerFactory.getLogger(FImagestoreConfigTest.class);
		
	@Test
	public void testGetConfig() throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException {
		try {
		TrpFImagestore f = client.getFImagestoreConfig();
		Assert.assertNotNull(f);
		logger.debug("Got fimagestore config: " + f);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

}
