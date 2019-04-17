package eu.transkribus.client.connection.jobs;

import java.util.List;

import org.junit.Test;

import eu.transkribus.client.ATrpClientTest;
import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor.PageDescriptor;
import eu.transkribus.core.model.beans.job.TrpJobStatus;

public class HtrTest extends ATrpClientTest {
	
//	@Test
	public void testHtrWithDescriptor() throws Exception {
		final int colId = 2;
		final int docId = 2278;
		final int pageId = 10070; //pageNr 1
		final int tsId = 25143;
		final String regionId1 = "r1";
		final String regionId2 = "r2";
		final int modelId = 241;
		
		//generate a page descriptor for a single page/single region HTR job
		DocumentSelectionDescriptor descriptor = new DocumentSelectionDescriptor(docId);
		PageDescriptor pd = new PageDescriptor(pageId, tsId);
		pd.getRegionIds().add(regionId1);
		pd.getRegionIds().add(regionId2);
		descriptor.addPage(pd);
		
		String jobId = client.runCitLabHtr(colId, descriptor, modelId, null);
		TrpJobStatus job = waitForJobToEnd(client.getJob(jobId));
		
	}
	
//	@Test
	public void testHtrWithDescriptorOnFaultyPage() throws Exception {
		final int colId = 2;
		final int docId = 6766;
		final int pageId = 24054; //pageNr 1, has a very short baseline
		final int tsId = 45696;
		
		final int modelId = 241;
		
		//generate a page descriptor for a single page HTR job
		DocumentSelectionDescriptor descriptor = new DocumentSelectionDescriptor(docId);
		PageDescriptor pd = new PageDescriptor(pageId, tsId);
		descriptor.addPage(pd);
		
		String jobId = client.runCitLabHtr(colId, descriptor, modelId, null);
		TrpJobStatus job = waitForJobToEnd(client.getJob(jobId));
		//this job fails with a job error in CITlabModule <= 2.3.0 due to the very short baseline.
	}
	
	public void startHtrTraining(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw)) {
			
			CitLabHtrTrainConfig conf = new CitLabHtrTrainConfig();
			
			conf.setDescription("");
			conf.setModelName("Goettingen");
			conf.setLanguage("German");
			
			conf.setNumEpochs(150);
			conf.setNoise("both");
			conf.setLearningRate("2e-3");
			conf.setTrainSizePerEpoch(1000);
			
			conf.setColId(4486);
			
			List<DocumentSelectionDescriptor> dds = super.getDds(4486);
			conf.getTrain().addAll(dds);
			
			System.out.println("conf = "+conf.toString());
			System.out.println("nr of docs = "+conf.getTrain().size());
			
			int c = 0;
			for (DocumentSelectionDescriptor dd : dds) {
				c += dd.getPages().size();
			}
			
			System.out.println("nf of pages = "+c);

			String jobID = conn.runCitLabHtrTraining(conf);
			System.out.println("Started training with jobId = "+jobID);
		}
	}
}
