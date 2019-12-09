package eu.transkribus.client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.ATrpModel;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpP2PaLA;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.core.util.ModelUtil;

public class ModelCallsTest {
	private static final Logger logger = LoggerFactory.getLogger(ModelCallsTest.class);
	
	@BeforeClass
	public static void initClient() throws IOException, LoginException {
		String ADMIN_CREDS_FILE_NAME = "adminCreds.properties";
		try (InputStream is = TrpServerConn.class.getClassLoader().getResourceAsStream(ADMIN_CREDS_FILE_NAME)) {
			if(is == null) {
				logger.warn("Could not find credentials file for test user: {}", ADMIN_CREDS_FILE_NAME);
			}
			//skip if no adminCreds file exists
			Assume.assumeNotNull("Skipping client test due to missing credentials file.", is);
		}
	}
	
	@Test
	public void testQueryModelToJsonAndCheckType() throws Exception {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			logger.info("Testing converting a model to json...");
			
			int modelId = 50161;
			TrpP2PaLA model = c.getModelCalls().getP2PaLAModel(modelId);
//			TrpP2PaLA model = new TrpP2PaLA();
//			model.setType(TrpP2PaLA.TYPE);
//			model.setType("helllooooo!");
			logger.info("model type = "+model.getType());
			String jsonStr = JaxbUtils.marshalToJsonString(model, true);
			logger.info("jsonStr =" +jsonStr);
			
			Map<String, String> map = ModelUtil.parseModelJsonAsMap(jsonStr);
			String type = map.get(ATrpModel.TYPE_COL);
			logger.info("type = "+type);
			
			Assert.assertEquals("type is invalid", TrpP2PaLA.TYPE, type);
			
//			logger.info("gsonStr =" +GsonUtil.toJson(model));
		}
	}
	
	@Test
	public void testSetModelDeleted() throws Exception {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			logger.info("Testing deleting a model...");
			
			int modelId = 50161;
//			TrpP2PaLA model = c.getModelCalls().getModel(modelId, TrpP2PaLA.TYPE);
			
//			model.setName("Changed name: "+new java.util.Date());
			c.getModelCalls().setModelDeleted(modelId);
			TrpP2PaLA model = c.getModelCalls().getP2PaLAModel(modelId);
			
			logger.info("model =" +model);
		}
	}
	
	@Test
	public void testUpdateModel() throws Exception {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			logger.info("Testing updating a model...");
			
			int modelId = 50161;
			TrpP2PaLA model = c.getModelCalls().getP2PaLAModel(modelId);
			
			model.setName("Changed ame: "+new java.util.Date());
			model.setDescription("Changed desc: "+new java.util.Date());
			model = c.getModelCalls().updateModel(model, TrpP2PaLA.class);
			
			logger.info("returned changed model =" +model);
		}
	}
	
	@Test
	public void testQueryModel() throws LoginException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			int modelId = 50161;
			
			TrpP2PaLA model = c.getModelCalls().getP2PaLAModel(modelId);
			logger.info("model = " +model);
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
