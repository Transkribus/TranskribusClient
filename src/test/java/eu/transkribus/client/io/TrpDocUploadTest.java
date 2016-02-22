package eu.transkribus.client.io;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.tuple.Pair;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.model.beans.TrpDoc;

public class TrpDocUploadTest {

	public static void main(String[] args) {

		final String BASE = "/mnt/dea_scratch/TRP/";
		final String docPath = BASE + "Bentham_box_035/";
		final String docPath2 = BASE + "TrpTestDoc_20140127/";
//		final String docPath3 = BASE + "test/I._ZvS_1902_4.Q";
//		final String docPath4 = BASE + "Schauplatz_Small2/";
		
//		String docPath4 = "/mnt/iza_retro/P6080-029-019_transcriptorium/master_images/14_bozen_stadtarchiv/Ratsprotokolle Bozen 1470-1684 - Lieferung USB Platte 9-7-2013/HS 37/HS 37a";
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], args[0], args[1])) {
			
//			PageXmlUtils.updatePageFormat(docPath);
			
			TrpDoc doc = LocalDocReader.load(docPath, true);
			System.out.println(doc.toString());

//			TrpServerConn conn = null;

//			conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[TrpServerConn.DEFAULT_URI_INDEX], args[0], args[1]);

			Observer o = new Observer(){
				private String resp;
			    public void update(Observable obj, Object arg) {
			        if (arg instanceof String) {
			            resp = (String) arg;
			            System.out.println("OBSERVER Update: " + resp);
			        }
			    }
			};
			
			conn.postTrpDoc(2, doc, null, false);
			
//			conn.status.addObserver(o);
			
//			Pair<TrpDocUploadMultipart, Thread> u = conn.postTrpDocMultipart(2, doc, o);
			
//			u.getLeft().addObserver(o);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
