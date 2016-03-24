package eu.transkribus.client.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;
import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.docx.DocxBuilder;
import eu.transkribus.core.model.builder.pdf.PdfExporter;
import eu.transkribus.core.model.builder.rtf.TrpRtfBuilder;

public class DocxTest {
	public static void main(String[] args) throws LoginException, MalformedURLException, DocumentException, IOException, JAXBException, URISyntaxException, Docx4JException{
		
		//TrpServerBentham - Batch2 - 363 pages
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], "philip", "test123");
		//blackening
		TrpDoc doc3 = conn.getTrpDoc(1, 43, -1);
		
		//Konzilsprotokolle - Test_ Textstyleoutput
		TrpDoc doc4 = conn.getTrpDoc(940, 2867, -1);
		
		//for tag output test
		TrpDoc doc5 = conn.getTrpDoc(940, 2444, -1);
		
		TrpDoc doc6 = conn.getTrpDoc(255, 3088, -1);
		
		TrpDoc doc7 = conn.getTrpDoc(211, 555, -1);
			
		@SuppressWarnings("serial")
		Set<Integer> idxs2 = new HashSet<Integer>() {{
			  add(1); add(2);
			}};
			
		conn.close();
		
		boolean wordbased = false;
		
	
		ExportUtils.storeCustomTagMapForDoc(doc7, wordbased, idxs2, null);
		DocxBuilder.writeDocxForDoc(doc7, true, true, true, new File("C:/Users/Administrator/DocxTest.rtf"), idxs2, null, CustomTagFactory.getRegisteredTagNames(), true, false, false);
		

	}
}
