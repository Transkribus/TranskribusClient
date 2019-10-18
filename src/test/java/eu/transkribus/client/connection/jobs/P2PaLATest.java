package eu.transkribus.client.connection.jobs;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.ATrpClientTest;
import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpP2PaLA;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.rest.P2PaLATrainJobPars;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.rest.JobConstP2PaLA;

public class P2PaLATest {
	private static final Logger logger = LoggerFactory.getLogger(P2PaLATest.class);
	
	public void testInsertP2PaLATrainJob() throws LoginException, IOException {
		Pair<Properties, TrpServerConn> p = ATrpClientTest.initClient("adminCreds.properties");
		
		try (TrpServerConn client = p.getRight()) {
			final int colId = 2;
			final int trainDocId = 6593; // stazh_p2pala_validation1
			final int testDocId = 6591; // stazh_p2pala_testset1
			
	//		final int pageId = 10070; //pageNr 1
	//		final int tsId = 25143;
	//		final String regionId1 = "r1";
	//		final String regionId2 = "r2";
	//		final int modelId = 241;
			
			//generate a page descriptor for a single page/single region HTR job
			List<DocumentSelectionDescriptor> trainDsds = DocumentSelectionDescriptor.fromDocIds(trainDocId);
			List<DocumentSelectionDescriptor> valDsds = DocumentSelectionDescriptor.fromDocIds(testDocId);
			List<DocumentSelectionDescriptor> testDsds = null;
			
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
		P2PaLATest t = new P2PaLATest();
		t.testInsertP2PaLATrainJob();
	}

}
