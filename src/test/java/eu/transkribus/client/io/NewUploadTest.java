package eu.transkribus.client.io;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpUpload.UploadType;

public class NewUploadTest {
	private static final Logger logger = LoggerFactory.getLogger(NewUploadTest.class);
	public static void main(String[] args) {

		final String BASE = "/mnt/dea_scratch/TRP/";
//		final String docPath = BASE + "Bentham_box_035/";
		
		final String docPath = BASE + "VeryLargeDocument/";
		
//		final String docPath2 = BASE + "TrpTestDoc_20140127/";
//		final String docPath3 = BASE + "test/I._ZvS_1902_4.Q";
//		final String docPath4 = BASE + "Schauplatz_Small2/";
		
//		String docPath4 = "/mnt/iza_retro/P6080-029-019_transcriptorium/master_images/14_bozen_stadtarchiv/Ratsprotokolle Bozen 1470-1684 - Lieferung USB Platte 9-7-2013/HS 37/HS 37a";
		
		try (TrpServerConn conn = new TrpServerConn(TrpServer.Test, args[0], args[1])) {
			
			TrpDoc doc = LocalDocReader.load(docPath, true);

			Observer o = new Observer(){
			    public void update(Observable obj, Object arg) {
			    	logger.info("OBSERVER Update: " + arg);
			    }
			};
			
			conn.uploadTrpDoc(2, doc, UploadType.METS, null, o);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
