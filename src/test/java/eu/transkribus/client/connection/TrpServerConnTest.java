package eu.transkribus.client.connection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.io.UnsupportedFormatException;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor.PageDescriptor;
import eu.transkribus.core.model.beans.PageLock;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpP2PaLAModel;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.builder.CommonExportPars;
import eu.transkribus.core.model.builder.alto.AltoExportPars;
import eu.transkribus.core.model.builder.docx.DocxExportPars;
import eu.transkribus.core.model.builder.pdf.PdfExportPars;
import eu.transkribus.core.model.builder.tei.TeiExportPars;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.core.util.SebisStopWatch;

public class TrpServerConnTest {
	private static final Logger logger = LoggerFactory.getLogger(TrpServerConnTest.class);
	
	static SebisStopWatch sw = new SebisStopWatch();
	
	public static void testDocMdDescriptionSizeLimit(String user, String pw) throws Exception {
		final int colId = 2;
		final int docId = 87;
		String testString = "";
		while (testString.length() < 4000) {
			testString += "REDRUM ";
		}
		testString = testString.substring(0, 4000);
		System.out.println("TestString size is: " + testString.length());
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			TrpDoc doc = conn.getTrpDoc(colId, docId, -1);
			TrpDocMetadata md = doc.getMd();
			md.setDesc(testString);
			conn.updateDocMd(colId, docId, md);
		}
	}
	
	public static void testGetMyDocsAsync(String user, String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServer.Test, user, pw)) {
//			Future<List<TrpDocMetadata>> f = conn.getAllDocsByUserAsync(0, 0, null, null, new InvocationCallback<List<TrpDocMetadata>>() {
//				@Override public void failed(Throwable throwable) {
//					logger.error("Error loading documents by user "+user+" - "+throwable.getMessage(), throwable);
//				}
//				
//				@Override public void completed(List<TrpDocMetadata> response) {
//					logger.debug("loaded docs by user "+user+" - "+response.size()+" thread: "+Thread.currentThread().getName());
//					
//				}
//			});
			
			conn.getAllDocsByUser(0, 0, null, null);
			
//			f.wait();
//			List<TrpDocMetadata> list = (List<TrpDocMetadata>)f;
//			logger.debug(list.size() + " documents");
		}
	}
	
	public static void testIsUserAllowedForJob(String user, String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			boolean isAllowed = conn.isUserAllowedForJob(JobImpl.CITlabLaJob.toString());
			System.out.println("isAllowed = "+isAllowed);
		}
	}
	
	public static void testSearchTags(final String user, final String pw) throws Exception  {
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw);
		
		int docid = 62;
		int cid = 2;
		int pid = 1;
		
//		String tagName = "abbrev";
		String tagName = "sic";
		String value = null;
		String regionType = "Line";
		
		Map<String, Object> attributes = new HashMap<>();
		
		boolean exactMatch = true;
		boolean caseSensitive = false;
		
		List<TrpDbTag> tags = conn.searchTags(CoreUtils.createSet(cid), null, null, tagName, value, regionType, exactMatch, caseSensitive, attributes);
		
		System.out.println("nr of selected tags: "+tags.size());
		tags.forEach((t) -> {
			System.out.println(t);
		});
		
	}
	
	public static void testBugReport(final String user, final String pw) throws Exception {
//		TrpServerConn conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[1], user, pw);
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1]);
		boolean sendCopy = true;
		boolean isBug = true;
		conn.sendBugReport("sebastian.colutto@uibk.ac.at", "testSubject!!", "massage", isBug, sendCopy, new File("/home/sebastianc/sebis_test.txt"));	
	}
	
	public static void testReplacePage(final String user, final String pw) throws LoginException {
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw);
		final String benthamPath = "/mnt/dea_scratch/TRP/test/justImages/035_321_001.jpg";
		final String path = "/mnt/dea_scratch/TRP/test/II._ZvS_1908_1.Q/ZS-II-1907-280 (1).jpg";
		File imgFile = new File(path);
		TrpPage page = conn.replacePageImage(2, 69, 1, imgFile, null);
		conn.close();
	}
	
	public static void testLoadHtrModels(final String user, final String pw) throws Exception {
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw);
		
		conn.getHtrModelListText();
		
	}
	
//	public static void testPostTranscript(int colId, int docId, int pageNr) {
//		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw);
//		final int pageNrToUpdate = 2;
//		TrpPage page = doc.getPages().get(pageNrToUpdate-1);
//		TrpTranscriptMetadata md = page.getTranscripts().get(0);
//		System.out.println(doc.getPages().getClass());
//
//		try {
//			JAXBPageTranscript j = TrpPageTranscriptBuilder.build(md);
//			PcGtsType p = j.getPageData();
//			TrpTranscriptMetadata newMd = conn.updateTranscript(colId, docId, pageNrToUpdate, EditStatus.IN_PROGRESS, p);
////			list = conn.updateTranscript(docId, pageNrToUpdate, EditStatus.IN_PROGRESS, null);
//			
//			System.out.println(newMd.toString());
//			
//			
//		} catch (JAXBException | IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	private static void testPostTranscript(final String user, final String pw) throws LoginException, UnsupportedFormatException, IOException, JAXBException {
		final String localPath = "/mnt/dea_scratch/tmp_alexandra/HS 37a";
		final int docId = 84;
		final int colId = 1;
		
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw);
		
		TrpDoc localDoc = LocalDocReader.load(localPath);
		
		for(TrpPage p : localDoc.getPages()){
			final int pageNr = p.getPageNr();
			if(pageNr >= 211 && pageNr <= 270){
				logger.debug("In page: " + pageNr);
				TrpTranscriptMetadata md = p.getTranscripts().get(0);
				PcGtsType pc = PageXmlUtils.unmarshal(md.getUrl());
				
				TrpTranscriptMetadata newMd = conn.updateTranscript(colId, docId, pageNr, EditStatus.IN_PROGRESS, pc, -1, null);
				
			}
		}
	}
	
	static void testFindDocs(final String user, final String pw)  throws Exception {
		TrpServerConn conn = new TrpServerConn(TrpServerConn.TEST_SERVER_URI, user, pw);
//		
//		List<TrpDocMetadata> docs = conn.findDocuments(0, 104, "", "", "", "", true, false, 0, 0);
		
		int colId = 0;
		Integer docId = null;
		String title = "b", description="", author="", writer="";
		boolean exactMatch=false, caseSensitive=false;
		int index=0, nValues=0;
		String sortFieldName="title", sortDirection="desc";
				
		sw.start();
		List<TrpDocMetadata> docs = conn.findDocuments(colId, docId, title, description, author, writer, exactMatch, caseSensitive, index, nValues, sortFieldName, sortDirection);
		sw.stop();
		logger.info("found "+docs.size()+" docs!");
		
		sw.start();
		int N = conn.countFindDocuments(colId, docId, title, description, author, writer, exactMatch, caseSensitive);
		sw.stop();
		logger.info("NTotal =  "+N);
		
		Assert.assertTrue("count and nr docs found mismatch!", docs.size()==N);
		
		if (true)
		for (TrpDocMetadata d : docs) {
			logger.info(""+d);
		}
	}
	
	private static void testSetTranscriptStatus(final String user, final String pw) throws LoginException, UnsupportedFormatException, IOException, JAXBException {
		final int[] docIds = {2683};
		final int colId = 255;
		
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw);
				
		for(final int docId : docIds){
			TrpDoc doc = conn.getTrpDoc(colId, docId, 1);
			
				int[] nrs = {1,2,3,35,60,61,117,118,159,166,167,168,169,170};
			
		//		for(int i = 0; i < nrs.length; i++){
			for(int i = 0; i < doc.getNPages(); i++) {
				final int pageNr = i+1;
				boolean isExcluded = false;
				
				for(int j : nrs){
					if(j == pageNr){
						logger.debug("Excluding page " + pageNr);
						isExcluded = true;
						break;
					}
				}
				
				if(!isExcluded){
					logger.debug("Set status on page " + pageNr);
					TrpTranscriptMetadata newMd = conn.updateTranscript(colId, docId, pageNr, EditStatus.IN_PROGRESS, null, -1, null);
				}
			}
		}
//		for(TrpPage p : doc.getPages()){
//			final int pageNr = p.getPageNr();
//			if(pageNr >= 1 && pageNr <= 100){
//				logger.debug("In page: " + pageNr);
//				
//				TrpTranscriptMetadata newMd = conn.updateTranscript(colId, docId, pageNr, EditStatus.DONE, null);
//				
//			}
//		}
		
		conn.close();
	}
	
	public static void testCountDocs(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			int cid = 0;
			int N = conn.countDocs(cid);
			System.out.println("cid= "+cid+" nDocs = "+N);	
		}
	}
	
	public static void testGetDocsPaging(final String user, final String pw) throws Exception {
		
		SebisStopWatch sw = new SebisStopWatch();
		
		int colId = 2;
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw)) {
			
			sw.start();
			List<TrpDocMetadata> docs = conn.getAllDocs(colId, 0, 0, null, null, false);
			sw.stop(true);
			System.out.println("got all docs: "+docs.size());
			
			
			sw.start();
			final int nDocs = conn.countDocs(2);
			int ps = 20;
			sw.stop(true);			
			System.out.println("nDocs = "+nDocs);
			
			for (int i=0; i<nDocs; i += ps) {
				System.out.println("i = "+i);
				
				sw.start();
				docs = conn.getAllDocs(colId, i, ps, null, null, false);
				sw.stop(true);
				System.out.println("got docs: "+docs.size());
			}
		}

//		sw.start();
//		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw)) {
////			List<TrpDocMetadata> docs = conn.getAllDocs(1, 0, 10, "title", "desc");
////			List<TrpDocMetadata> docs = conn.getAllDocs(2, 0, 0, "title", "desc");
////			List<TrpDocMetadata> docs = conn.getAllDocs(2, -1, -1, "title", "desc");
//			List<TrpDocMetadata> docs = conn.getAllDocs(2, -1, -1, null, null);
////			List<TrpDocMetadata> docs = conn.getAllDocs(2, 0, 10, null, null);
//			System.out.println("got "+docs.size()+" docs");
//			
////			for (TrpDocMetadata m : docs) {
////				System.out.println(m);
////			}
//		}
//		sw.stop();
	}
	
	static void testUsersForCollectionPaging(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			List<TrpUser> list = conn.getUsersForCollection(3, null, 2, 7, null, null);
			System.out.println("N = "+list.size());
			for (TrpUser m : list) {
				System.out.println(m);
			}
		}
	}
	
	static void testCountUsersForCollection(final String user, final String pw) throws Exception {
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			int N = conn.countUsersForCollection(4, null);
			System.out.println("N = "+N);
		}
		
	}
	
	static void testCountMyDocs(final String user, final String pw) throws Exception {
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			int N = conn.countMyDocs();
			System.out.println("N = "+N);
		}
		
		
	}

	static void testListCollectionsPaging(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			int N = conn.countAllCollections();
			System.out.println("N = "+N);
			List<TrpCollection> list = conn.getAllCollections(1, 100, null, null);
			for (TrpCollection c : list) {
				System.out.println(""+c);
			}
		}
	}
	
	static void testJobsPagination(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			SebisStopWatch sw = new SebisStopWatch();
			
			int N = conn.countJobs(true, null, null, null);
			sw.stop(true);

			System.out.println("Njobs = "+N);
			sw.start();
			List<TrpJobStatus> list = conn.getJobs(false, null, null, null, 2, 4, null, null);
			sw.stop(true);
			for (TrpJobStatus j : list) {
				System.out.println(""+j);
			}
			
		}
	}
	
	static void testGetJobsByType(String user, String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			List<TrpJobStatus> jobs = conn.getJobs(true, TrpJobStatus.UNFINISHED, "Export Document", null, 0, 0, null, null);
			System.out.println("got jobs: "+jobs.size());
			for (TrpJobStatus j : jobs) {
				System.out.println(""+j);
			}		
		}
	}
	
	static void testTranscriptsPagination(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			SebisStopWatch sw = new SebisStopWatch();
			
			int N = conn.countTranscriptMdList(2, 62, 2);
			sw.stop(true);

			System.out.println("Ntranscripts = "+N);
			sw.start();
			List<TrpTranscriptMetadata> list = conn.getTranscriptMdList(2, 62, 1, 1, 19, null, null);
			sw.stop(true);
			for (TrpTranscriptMetadata j : list) {
				System.out.println(""+j);
			}
		}
		
	}
	
	static void testListingPageLocks(final String user, final String pw) throws Exception {
//		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw)) {
			List<PageLock> pls = conn.listPageLocks(-1, -1, -1);
			
			logger.info("got "+pls.size()+" page locks");
			for (PageLock pl : pls) {
				logger.info(""+pl);
			}

		}
	}
	
//	static void testListSessions() throws Exception {
////		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
//		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw)) {
//			
//			
//			logger.info("got "+pls.size()+" page locks");
//			for (PageLock pl : pls) {
//				logger.info(""+pl);
//			}
//
//		}
//	}
	
	static void testDeleteUser(String username, String user, String pw) throws ServerErrorException, ClientErrorException, LoginException {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw)) {
			conn.deleteUser(username);
			System.out.println("success deleting user: "+username);
		}
	}
	
	static void testUpdatePageStatus(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			conn.updatePageStatus(2, 681, 1, 8269, EditStatus.DONE, "A test");
		}
	}
	
	static void testServerExport(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
//			conn.exportDocument(2, 1312, "1-31", true, true, true, true, false, true, true, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, false, false, true, false, false, false, "Latest");
			
			CommonExportPars commonPars = new CommonExportPars();
			commonPars.setDoWriteTei(true);
			commonPars.setDoWritePdf(true);
			commonPars.setDoWriteDocx(true);
			commonPars.setDoWriteTxt(true);
			commonPars.setDoWriteTagsXlsx(true);
			commonPars.setDoWriteTablesXlsx(true);
			
			AltoExportPars altoPars = new AltoExportPars();
			PdfExportPars pdfPars = new PdfExportPars();
			TeiExportPars teiPars = new TeiExportPars();
			DocxExportPars docxPars = new DocxExportPars();
			
//			String jobID = conn.exportDocument(2, 1312, commonPars, altoPars, pdfPars, teiPars, docxPars);
//			System.out.println("Started export job "+jobID);
			
			List<DocumentSelectionDescriptor> dsds = new ArrayList<>();
			dsds.add(new DocumentSelectionDescriptor(1732));
			dsds.add(new DocumentSelectionDescriptor(1676));
//			dsds.add(new DocumentSelectionDescriptor(123456789)); // does not exist!
			String jobID = conn.exportDocuments(2, dsds, commonPars, altoPars, pdfPars, teiPars, docxPars);
			System.out.println("Started export job "+jobID);
		}
	}
	
	static List<DocumentSelectionDescriptor> getDds(TrpServerConn conn, int collId) throws Exception {
		List<TrpDocMetadata> docMds = conn.getAllDocs(collId, 0, 0, null, null, false);
		System.out.println("n-docs = "+docMds.size());
		List<DocumentSelectionDescriptor> dds = new ArrayList<>();
		
		for (TrpDocMetadata dm : docMds) {
			TrpDoc d = conn.getTrpDoc(collId, dm.getDocId(), 1);
			
			DocumentSelectionDescriptor dd = new DocumentSelectionDescriptor();
			dd.setDocId(d.getId());
			
			for (TrpPage p : d.getPages()) {
				PageDescriptor pd = new PageDescriptor();
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
				
//				System.out.println(""+tmd);
				
				pd.setPageId(p.getPageId());
				pd.setTsId(tmd.getTsId());
				
				dd.getPages().add(pd);
			}
			
			dds.add(dd);
		}
		
		return dds;
	}
	
	static void startHtrTraining(final String user, final String pw) throws Exception {
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
			
			List<DocumentSelectionDescriptor> dds = getDds(conn, 4486);
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
			
//			conn.exportDocument(2, 1312, "1-31", true, true, true, true, false, true, true, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, false, false, true, false, false, false, "Latest");
		}
	}
	
	public static void testGetStringListTest(String user, String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {	
			List<String> list = conn.getStringListTest();			
			System.out.println("list = "+CoreUtils.join(list));
		}
	}
	
	public static void testStartLa(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {			
			int colId = 2;
			Integer docId = 62;
			Integer pageId = 1;
//			1/21303
			Integer tsId = 21303;
			
			DocumentSelectionDescriptor dd1 = new DocumentSelectionDescriptor(docId, pageId);
			
			DocumentSelectionDescriptor dd2 = new DocumentSelectionDescriptor(63, 321);
			dd2.getPages().get(0).setTsId(1234);
			dd2.getPages().get(0).getRegionIds().add("r1");
			dd2.getPages().get(0).getRegionIds().add("rasdfasdf_asdfasdf");
			
			List<DocumentSelectionDescriptor> dds = new ArrayList<>();
			dds.add(dd1);
//			dds.add(dd2);
			
//			List<TrpJobStatus> jobs = conn.analyzeLayout(2, dds, true, true, false, JobImpl.CITlabLaJob, null);
			List<TrpJobStatus> jobs = conn.analyzeLayout(2, dds, false, false, false, true, false, JobImpl.NcsrLaJob.toString(), null);
			
			System.out.println("jobs inserted: "+jobs.size());
			for (TrpJobStatus j : jobs) {
				System.out.println(j);
			}
		}
	}
	
	public static void testStartBaseline2Polygon(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {			
			int colId = 1;
			Integer docId = 447;
			Integer pageId = 2249;
//			1/21303
			Integer tsId = 5155;
			
			DocumentSelectionDescriptor dd1 = new DocumentSelectionDescriptor(docId, pageId);
			
//			DocumentSelectionDescriptor dd2 = new DocumentSelectionDescriptor(63, 321);
//			dd2.getPages().get(0).setTsId(1234);
//			dd2.getPages().get(0).getRegionIds().add("r1");
//			dd2.getPages().get(0).getRegionIds().add("rasdfasdf_asdfasdf");
			
			List<DocumentSelectionDescriptor> dds = new ArrayList<>();
			dds.add(dd1);
//			dds.add(dd2);
			
			List<TrpJobStatus> jobs = conn.analyzeLayout(colId, dds, false, false, false, false, true, JobImpl.NcsrOldLaJob.toString(), null);
			
			System.out.println("jobs inserted: "+jobs.size());
			for (TrpJobStatus j : jobs) {
				System.out.println(j);
			}
		}
	}
	
	public static void testStartPolygon2Baseline(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {			
			int colId = 1;
			Integer docId = 447;
			Integer pageId = 2249;
//			1/21303
			Integer tsId = 5155;
			
			DocumentSelectionDescriptor dd1 = new DocumentSelectionDescriptor(docId, pageId);
			
//			DocumentSelectionDescriptor dd2 = new DocumentSelectionDescriptor(63, 321);
//			dd2.getPages().get(0).setTsId(1234);
//			dd2.getPages().get(0).getRegionIds().add("r1");
//			dd2.getPages().get(0).getRegionIds().add("rasdfasdf_asdfasdf");
			
			List<DocumentSelectionDescriptor> dds = new ArrayList<>();
			dds.add(dd1);
//			dds.add(dd2);
			
//			List<TrpJobStatus> jobs = conn.analyzeLayout(2, dds, true, true, false, JobImpl.CITlabLaJob, null);
			List<TrpJobStatus> jobs = conn.analyzeLayout(colId, dds, false, false, false, true, false, JobImpl.NcsrOldLaJob.toString(), null);
			
			System.out.println("jobs inserted: "+jobs.size());
			for (TrpJobStatus j : jobs) {
				System.out.println(j);
			}
		}
	}
	
	public static void testStartText2Image(final String user, final String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], user, pw)) {
			
//			DocumentSelectionDescriptor dd1 = new DocumentSelectionDescriptor(459); // bentham
//			dd1.addPage(new PageDescriptor(3082));
			
//			DocumentSelectionDescriptor dd1 = new DocumentSelectionDescriptor(2092); // orient
//			dd1.addPage(new PageDescriptor(9521));
			
			DocumentSelectionDescriptor dd1 = new DocumentSelectionDescriptor(26250); // orient on prod server
//			dd1.addPage(new PageDescriptor(9521));
			
			CitLabSemiSupervisedHtrTrainConfig conf = new CitLabSemiSupervisedHtrTrainConfig();
			conf.setColId(1);
			conf.getTrain().add(dd1);
//			conf.setLanguage("English");
			conf.setLanguage("Arabic");
			conf.setTrainEpochs("1;1;2;3;5;8;13;21;34;55");
			conf.setNoise("both");
			conf.setLearningRate("2e-3");
			conf.setTrainSizePerEpoch(2000);
			conf.setnThreads(12);
//			conf.setBaseModelId(101); // bentham
			conf.setBaseModelId(624); // arabic on prod server

			String jobID = conn.runCitLabText2Image(conf);
			System.out.println("Started Text2Image with jobId = "+jobID);
		}
	}
	
	public static void testGetP2PaLAModelsOld(String user, String pw) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			List<TrpP2PaLAModel> models = conn.getP2PaLAModelsOld(1);
			System.out.println("Got models: "+models.size());
			models.stream().forEach(System.out::println);
		}
	}
	
	public static void testGetImageNames(String user, String pw) throws Exception {
		int colId = 2875;
		int docId = 7645;
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
//		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.PROD_SERVER_URI, user, pw)) {
			List<String> imageNames = conn.getImageNames(colId, docId);
			imageNames.stream().forEach(i -> System.out.println(i));
		}
	}
	
	public static void testMoveImagesByName(String user, String pw) throws Exception {
		File filelist = new File("./src/test/resources/filelist_2875_7645.txt");
		int colId = 2875;
		int docId = 7645;
		
		if (!filelist.exists()) {
			throw new FileNotFoundException("Filelist not found: "+filelist.getAbsolutePath());
		}
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], user, pw)) {
			conn.moveImagesByNames(colId, docId, filelist);
		}
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
//		testGetImageNames(args[0], args[1]);
		
		testMoveImagesByName(args[0], args[1]);
		
//		testGetP2PaLAModels(args[0], args[1]);
		
//		testDocMdDescriptionSizeLimit(args[0], args[1]);
		
//		testIsUserAllowedForJob(args[0], args[1]);
		
//		testStartLa(args[0], args[1]);
		
//		testStartPolygon2Baseline(args[0], args[1]);
		
//		startHtrTraining(args[0], args[1]);
		
//		testDeleteUser("fivarep@axon7zte.com", args[0], args[1]);
		
//		testListingPageLocks(args[0], args[1]);
		
//		testFindDocs(args[0], args[1]);
		
//		testTranscriptsPagination(args[0], args[1]);
		
//		testJobsPagination(args[0], args[1]);
		
//		testListCollectionsPaging(args[0], args[1]);
		
//		testCountUsersForCollection(args[0], args[1]);
		
//		testCountMyDocs(args[0], args[1]);
		
//		testUsersForCollectionPaging(args[0], args[1]);
		
//		testCountDocs(args[0], args[1]);
		
//		testGetDocsPaging(args[0], args[1]);
		
//		testSetTranscriptStatus(args[0], args[1]);
		
//		testLoadHtrModels(args[0], args[1]);
		
//		testFindDocs(args[0], args[1]);
		
//		testReplacePage(args[0], args[1]);
		
//		testBugReport(args[0], args[1]);
		
//		testPostTranscript(args[0], args[1]);
		
//		testUpdatePageStatus(args[0], args[1]);
		
//		testServerExport(args[0], args[1]);
		
//		testGetJobsByType(args[0], args[1]);
		
//		testStartText2Image(args[0], args[1]);
		
//		testSearchTags(args[0], args[1]);
	
//		testGetMyDocsAsync(args[0], args[1]);
		
		if (true)
			return;
		
		final int docId = 69;

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[TrpServerConn.DEFAULT_URI_INDEX], args[0], args[1])) {			
			TrpDoc doc = conn.getTrpDoc(2, docId, 1);
			
//			TrpDocMetadata md = doc.getMd();
//			md.setTitle("Bozen/Reichsgericht/Zwettl Testdoc");
//			conn.updateDocMd(docId, md);
			
//			System.out.println(doc.toString());
			System.out.println("\n\n");
			
//			List<TrpDocMetadata> mdList = conn.getAllDocs();
//			
//			for(TrpDocMetadata m : mdList){
//				System.out.println(m.toString());
//			}
//			
			List<TrpTranscriptMetadata> list = conn.getTranscriptMdList(2, docId, 1, 0, 3, null, null);
			for(TrpTranscriptMetadata m : list){
				System.out.println(m.toString());
				try {
					conn.lockPage(2, 69, 1);
//					System.out.println(JaxbUtils.marshalToString(pc));
					boolean isLocked = conn.isPageLocked(2, 69, 1);
					System.out.println(isLocked);
//					break;
				} catch (ServerErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//				} catch (JAXBException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
			
//			doc = conn.getTrpDoc(docId);
//			
//			System.out.println(doc.toString());			
		} catch (LoginException e){
			e.printStackTrace();
		}
		
		
//		System.out.println("Received doc from REST service:");
		
//		List<TrpDocMetadata> list = conn.getAllDocs();
//		
//		if(list!= null && !list.isEmpty()){
//			for(TrpDocMetadata doc : list){
//				System.out.println(doc);
//			}
//		}
		
//		TrpDoc doc = conn.getFakeDoc();
		
//		//from service 
//		String oans = doc.toString();
//		System.out.println(oans);
//		
//		System.out.println("\nOriginal doc was:");
//		//original doc
//		String zwoa = FakeDocProvider.create(false).toString();
//		System.out.println(zwoa);
//		
//		System.out.println("Is equal after submission to client? " + oans.equals(zwoa) + "\n");
//		
//		System.out.println("Sorted pages:");
//		List<TrpPage> pages = doc.getPages();
//		
//		Collections.sort(pages);
//		for(TrpPage p : pages){
//			System.out.println(p.getPageNr());
//		}
		
		
//		TrpDocMetadata docMd = conn.getTrpDoc(47).getMd();
//		System.out.println(docMd.toString());
//		docMd.setGenre("Text from REST client update");
//		conn.updateDocMd(47, docMd);
		
	}
}
