package eu.transkribus.client.connection;

import java.io.IOException;
import java.util.Date;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.dea.fimgstoreclient.FimgStoreGetClient;
import org.dea.fimgstoreclient.beans.FimgStoreXml;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.util.JaxbUtils;

public class LaTest {
	
	final static String imgKey = "RVGYYINSPWXARRAKMFRZYFFM";
	final static String xmlKey = "MWHSHDRUWZNVHSYCENGDNCUP";
	
	
//	final static String imgKey = "ZAKOVQKUJKVTDAHPECENORMT";
//	final static String xmlKey = "UZXXQUQOAHTBRAORXMJYWAYS";
	
//	exit code 143: img = DXWHUCGLHAQFCZUYJDORVJXF | xml = NFRNBLMWTSAMVJQYIVACERCS
	
	final static String regionId = "r1";
//	final static String regionId2 = "r20";
	
	final static int docId = 63; //62
	
	public static void main(String[] args){
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
//		TrpServerConn conn = null;

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], args[0], args[1])) {
//			conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[TrpServerConn.DEFAULT_URI_INDEX], args[0], args[1]);
			
			FimgStoreGetClient getter = new FimgStoreGetClient("dbis-thure.uibk.ac.at", "f");
			
//			List<String> idList = new ArrayList<>(2);
//			idList.add(regionId);
//			idList.add(regionId2);
			
			TrpDoc doc = conn.getTrpDoc(2, docId, 1);
			
			TrpPage p = doc.getPages().get(1);
			
			final String imgK = p.getKey();
			final String xmlK = p.getCurrentTranscript().getKey();
			System.out.println("img = " + imgK + " | xml = " + xmlK);
			FimgStoreXml xml = getter.getXml(xmlK);
			PcGtsType pc = JaxbUtils.unmarshal(xml.getXmlDoc(), PcGtsType.class);
			long start = new Date().getTime();
			final String jobId = conn.analyzeBlocks(2, docId, p.getPageNr(), pc, true);
			System.out.println(jobId);
			long end = new Date().getTime();
			long time = end-start;
			float timeInSec = (time/1000.0f);
			System.out.println("Time: " + timeInSec + " seconds");
			
			try {
				while(true){
					TrpJobStatus job = conn.getJob(jobId);
					System.out.println(job.toString());
					if(job.getEndTime() > 0){
						break;
					}
					Thread.sleep(3000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
//			FimgStoreXml xml = getter.getXml(xmlKey);
//			PcGtsType pc = JaxbUtils.unmarshal(xml.getXmlDoc(), PcGtsType.class);
//			long start = new Date().getTime();
//			PcGtsType newPc = conn.analyzeSegmentation(imgKey, pc, idList);
////			PcGtsType newPc = conn.analyzeSegmentation(imgKey, pc, null);
//			long end = new Date().getTime();
//			
//			long time = end-start;
//			float timeInSec = (time/1000.0f);
//			
//			System.out.println("Time: " + timeInSec + " seconds");
//			JaxbUtils.marshalToSysOut(newPc);
			
		} catch (LoginException e){
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
