package eu.transkribus.client.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.builder.pdf.PdfExporter;

public class PdfTest {
	private static final Logger logger = LoggerFactory.getLogger(PdfTest.class);
	public static void main(String[] args) throws LoginException, MalformedURLException, DocumentException, IOException, JAXBException, URISyntaxException, InterruptedException{
//		TrpServerConn conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[1], args[0], args[1]);
//		TrpDoc doc = conn.getTrpDoc(69);
		
//		//TrpServerBentham - Batch2 - 363 pages
		logger.debug("TrpServerConn.SERVER_URIS[0] " + TrpServerConn.SERVER_URIS[0]);
		logger.debug("args[0] " + args[0]);
		logger.debug("args[1] " + args[1]);
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], args[0], args[1]);
		TrpDoc doc = conn.getTrpDoc(15277, 25840, -1);
		conn.close();
		
		boolean exportOrigImage = false;
		
		(new PdfExporter()).export(doc, "/tmp/canbuffi_view.pdf", null, null, exportOrigImage);
	}
}
