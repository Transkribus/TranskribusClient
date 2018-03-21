package eu.transkribus.client.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;
import javax.xml.bind.JAXBException;

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.builder.pdf.PdfExporter;

public class PdfTest {
	public static void main(String[] args) throws LoginException, MalformedURLException, DocumentException, IOException, JAXBException, URISyntaxException, InterruptedException{
//		TrpServerConn conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[1], args[0], args[1]);
//		TrpDoc doc = conn.getTrpDoc(69);
		
//		//TrpServerBentham - Batch2 - 363 pages
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[2], args[0], args[1]);
		TrpDoc doc = conn.getTrpDoc(2, 107, -1);
		conn.close();
		
		(new PdfExporter()).export(doc, "/tmp/doc107.pdf", null, null);
	}
}
