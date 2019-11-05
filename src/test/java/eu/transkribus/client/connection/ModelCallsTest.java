package eu.transkribus.client.connection;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpP2PaLA;

public class ModelCallsTest {
	private static final Logger logger = LoggerFactory.getLogger(ModelCallsTest.class);
	
	@Test
	public void testUpdateModel() throws LoginException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			logger.info("Testing updating a model...");
			
			int modelId = 50161;
			TrpP2PaLA model = c.getModelCalls().getModel(modelId, TrpP2PaLA.TYPE);
			model.setName("NAME CHANGED!!");
			c.getModelCalls().updateModel(model, TrpP2PaLA.TYPE);
			
			logger.info("model =" +model);
		}
	}
	
	@Test
	public void testQueryModel() throws LoginException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			int modelId = 50161;
			
			TrpP2PaLA model = c.getModelCalls().getModel(modelId, TrpP2PaLA.TYPE);
			logger.info("model =" +model);
		}
	}
	
	@Test
	public void testQueryModels() throws LoginException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			int modelId = 50161;
			
			boolean allModels=true;
			Integer colId=null;
			Integer userId=null;
			Integer releaseLevel=null;
			
			List<TrpP2PaLA> models = c.getModelCalls().getModels(true, allModels, colId, userId, releaseLevel, TrpP2PaLA.TYPE);
			logger.info("n-models =" +models.size());
			for (TrpP2PaLA m : models) {
				logger.info("m = "+m);
			}
		}
	}
	
	@Test
	public void testAddOrRemoveModelToCollection() throws TrpServerErrorException, TrpClientErrorException, LoginException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			int modelId = 50161;
			int colId = 3;
			boolean delete=false;
			
//			c.getModelCalls().addOrRemoveModelFromCollection(modelId, colId, true);
			
			List<Integer> colIds = c.getModelCalls().getModelCollectionIds(modelId);
			logger.info("colIds1 = "+colIds);
			
			c.getModelCalls().addOrRemoveModelFromCollection(modelId, colId, delete);
			
			colIds = c.getModelCalls().getModelCollectionIds(modelId);
			logger.info("colIds2 = "+colIds);
			
			c.getModelCalls().addOrRemoveModelFromCollection(modelId, colId, true);
			
			colIds = c.getModelCalls().getModelCollectionIds(modelId);
			logger.info("colIds3 = "+colIds);
			
//			List<Integer> colIds = c.getModelCalls().getModelCollections(modelId);
//			logger.info("resp = "+resp);
		}
	}
	
	@Test
	public void testQueryModelCollections() throws TrpServerErrorException, TrpClientErrorException, LoginException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			int modelId = 50161;
			
			List<Integer> colIds = c.getModelCalls().getModelCollectionIds(modelId);
			logger.info("colIds = "+colIds);
			
			List<TrpCollection> colls = c.getModelCalls().getModelCollections(modelId);
			logger.info("colls = "+colls);
		}
	}
}
