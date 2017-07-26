package eu.transkribus.client.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.xml.bind.JAXBException;

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.pdf.PdfExporter;
import eu.transkribus.core.model.builder.rtf.TrpRtfBuilder;

public class PdfTestCopy {
	public static void main(String[] args) throws LoginException, MalformedURLException, DocumentException, IOException, JAXBException, URISyntaxException, InterruptedException{
//		TrpServerConn conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[1], "philip", "test123");
//		TrpDoc doc = conn.getTrpDoc(69);
		
//		//TrpServerBentham - Batch2 - 363 pages
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], "philip", "test123");
		TrpDoc doc = conn.getTrpDoc(606, 3230, -1);
		TrpDoc doc2 = conn.getTrpDoc(1394, 3351, -1);
		//doc3 = Ruma
		TrpDoc doc3 = conn.getTrpDoc(255, 3088, -1);
		TrpDoc doc33 = conn.getTrpDoc(640, 5446, -1);
		//doc4 = Konzilsprotokolle
		TrpDoc doc4 = conn.getTrpDoc(940, 2444, -1);
		TrpDoc doc5 = conn.getTrpDoc(211, 555, -1);
		TrpDoc doc6 = conn.getTrpDoc(1885, 3686, -1);
		conn.close();

		@SuppressWarnings("serial")
		Set<Integer> idxs1 = new HashSet<Integer>() {{
			add(0);add(1);add(2);add(3);add(4);add(5);add(6);add(7);
			}};
			
		@SuppressWarnings("serial")
		Set<Integer> idxs2 = new HashSet<Integer>() {{
			add(219);add(220);
			}};
			
		boolean wordbased = false;
			
		ExportUtils.storeCustomTagMapForDoc(doc33, wordbased, idxs2, null, false);
		(new PdfExporter()).export(doc33, "C:/Users/Schorsch/arabic_test.pdf", idxs2);
		
		//TrpRtfBuilder.writeRtfForDoc(doc4, false, new File("C:/Users/Administrator/KonzilsProtokolle_test2.rtf"), idxs2, null);
		//(new PdfExporter()).export(doc2, "C:/Users/Administrator/Reichsgericht_test.pdf", idxs2);

		//(new PdfExporter()).export(doc, "/tmp/doc107.pdf", null);

	}
}
