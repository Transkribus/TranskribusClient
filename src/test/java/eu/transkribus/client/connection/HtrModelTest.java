package eu.transkribus.client.connection;
import java.util.List;
import java.util.concurrent.Future;

import javax.ws.rs.client.InvocationCallback;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.ATrpClientTest;
import eu.transkribus.core.model.beans.ReleaseLevel;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.rest.TrpHtrList;
import eu.transkribus.core.util.HtrCITlabUtils;

public class HtrModelTest extends ATrpClientTest {
	private static final Logger logger = LoggerFactory.getLogger(HtrModelTest.class);
		
	@Test
	public void testGetModel() throws Exception {
		try {
			List<TrpCollection> colList = client.getAllCollections(0, -1, null, null);
			Assume.assumeFalse("No collections could be retrieved!", CollectionUtils.isEmpty(colList));
			
			TrpCollection col = colList.get(0);
			final int colId = col.getColId();
			logger.info("Trying to retrieve models in collection {}", colId);
			
			Future<TrpHtrList> htrListFut = client.getHtrs(colId, HtrCITlabUtils.PROVIDER_CITLAB_PLUS, 0, -1, new InvocationCallback<TrpHtrList>() {
				@Override
				public void completed(TrpHtrList response) {
					logger.info("Success retrieving modelList!");
				}
				@Override
				public void failed(Throwable throwable) {
					Assert.fail("Failure retrieving modelList: " + throwable.getMessage());
				}
			});
			TrpHtrList htrList = htrListFut.get();
			logger.info("Retrieved {} models from server", htrList.getList().size());
			for(TrpHtr htr : htrList.getList()) {
				
				
				if(htr.getReleaseLevelValue() > 0 && htr.getCollectionIdLink() == null) {
					logger.info("Found public model with ReleaseLevel = {}", htr.getReleaseLevel());
					if(ReleaseLevel.isPrivateDataSet(htr.getReleaseLevel())) {
						//we should not be able to retrieve the datasets for this model!
						try {
							List<TrpGroundTruthPage> gtPages = client.getHtrTrainData(colId, htr.getHtrId());
							Assert.fail("Could retrieve {} gtPages for public model with private datasets!");
						} catch (Exception e) {
							//this is fine
							logger.debug("Retrieving datasets for public model ID = {} with private datasets failed as expected.", htr.getHtrId(), e.getMessage());
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
}
