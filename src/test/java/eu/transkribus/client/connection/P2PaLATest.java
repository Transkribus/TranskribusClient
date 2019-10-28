package eu.transkribus.client.connection;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.DocSelection;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpP2PaLA;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.rest.P2PaLATrainJobPars;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.rest.JobConstP2PaLA;
import eu.transkribus.core.util.DocSelectionUtil;

public class P2PaLATest {
	private static final Logger logger = LoggerFactory.getLogger(P2PaLATest.class);
	
	@Test
	public void testQueryP2PaLAModels() throws LoginException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn c = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			int colId = 2815;
			
			List<TrpP2PaLA> models = c.getP2PaLAModels(true, false, colId, null, null);
			logger.debug("got "+models.size()+" models colId = "+colId);
			
			models = c.getP2PaLAModels(true, true, colId, null, null);
			logger.debug("got "+models.size()+" models (all models)");
			
			models = c.getP2PaLAModels(true, false, colId, -1234, null);
			logger.debug("got "+models.size()+" models (invalid userId)");
			
			models = c.getP2PaLAModels(true, false, null, 42, null);
			logger.debug("got "+models.size()+" models (userId=42)");
			
			models = c.getP2PaLAModels(true, false, null, null, 1);
			logger.debug("got "+models.size()+" models (releaseLevel>=1)");	
		}
	}
	
	public void testInsertP2PaLATrainJob() throws LoginException, IOException {
		TrpProperties creds = new TrpProperties("adminCreds.properties");
		try (TrpServerConn client = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
			final int colId = 2;
			final int trainDocId = 6593; // stazh_p2pala_validation1
			final int testDocId = 6591; // stazh_p2pala_testset1
			
	//		final int pageId = 10070; //pageNr 1
	//		final int tsId = 25143;
	//		final String regionId1 = "r1";
	//		final String regionId2 = "r2";
	//		final int modelId = 241;
			
			//generate a page descriptor for a single page/single region HTR job
//			List<DocumentSelectionDescriptor> trainDsds = DocumentSelectionDescriptor.fromDocIds(trainDocId);
//			List<DocumentSelectionDescriptor> valDsds = DocumentSelectionDescriptor.fromDocIds(testDocId);
//			List<DocumentSelectionDescriptor> testDsds = null;
			List<DocSelection> trainDsds = DocSelectionUtil.fromDocIds(trainDocId);
			List<DocSelection> valDsds = DocSelectionUtil.fromDocIds(testDocId);
			List<DocSelection> testDsds = null;
			
			P2PaLATrainJobPars jobPars = new P2PaLATrainJobPars();
			jobPars.setTrainDocs(trainDsds);
			jobPars.setValDocs(valDsds);
			jobPars.setTestDocs(testDsds);
			
			jobPars.getParams().addParameter(JobConst.PROP_MODELNAME, "testModel_"+System.currentTimeMillis());
			jobPars.getParams().addParameter(JobConst.PROP_NR_OF_THREADS, 3);
			jobPars.getParams().addParameter(JobConst.PROP_DESCRIPTION, "i am a test description!!");
			jobPars.getParams().addParameter(JobConstP2PaLA.PROP_REGIONS, "paragraph heading footnote");
			jobPars.getParams().addParameter(JobConstP2PaLA.PROP_MERGED_REGIONS, "heading:header footnote:footnote-continued");
			jobPars.getParams().addParameter(JobConstP2PaLA.PROP_OUT_MODE, TrpP2PaLA.OUT_MODE_REGIONS_ONLY);
			
			String jobId = client.trainP2PaLAModel(colId, jobPars);
			logger.info("created job with id = "+jobId);
			
			TrpJobStatus job = client.getJob(jobId);
			logger.info(""+job);
		}
	}
	
	public static void main(String[] args) throws Exception {
//		P2PaLATest t = new P2PaLATest();
//		t.testInsertP2PaLATrainJob();
	}

}
