package eu.transkribus.client.connection;

import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.ATrpClientTest;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpAction;

public class ActionsTest extends ATrpClientTest {
	private static final Logger logger = LoggerFactory.getLogger(ActionsTest.class);
	
	@Test
	public void testGetMostRecentActions() throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException {
		TrpAction a = client.getMostRecentDocumentAction();
		Assert.assertNotNull(a);
		logger.debug("Got most recent action: " + a);
	}
	
	@Test
	public void testListActions() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		final int colId = 2;
		final int nValues = 10;
		List<TrpAction> aList = client.listActions(1, colId, null, nValues);
		Assert.assertEquals(nValues, aList.size());
		for(int i = 0; i < aList.size(); i++) {
			Assert.assertEquals(aList.get(i).getColId(), Integer.valueOf(colId));
		}
		
	}

}
