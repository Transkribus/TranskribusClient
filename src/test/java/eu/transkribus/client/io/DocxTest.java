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
import javax.ws.rs.ClientErrorException;
import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.docx.DocxBuilder;
import eu.transkribus.core.model.builder.pdf.PdfExporter;
import eu.transkribus.core.model.builder.rtf.TrpRtfBuilder;

public class DocxTest {
	public static void main(String[] args){
		
		
		//TrpServerBentham - Batch2 - 363 pages
		try {
			TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], "philip", "test123");
			//blackening
			TrpDoc doc3 = conn.getTrpDoc(1, 40, -1);
			
			//Konzilsprotokolle - Test_ Textstyleoutput
			TrpDoc doc4 = conn.getTrpDoc(940, 2867, -1);
			
			//for tag output test
			TrpDoc doc5 = conn.getTrpDoc(940, 2444, -1);
			
			TrpDoc doc6 = conn.getTrpDoc(255, 3088, -1);
			
			//for arabicoutput test
			TrpDoc doc7 = conn.getTrpDoc(640, 5446, -1);
				
			@SuppressWarnings("serial")
			Set<Integer> idxs2 = new HashSet<Integer>() {{
				  add(220); add(221);
				}};
				
			conn.close();
			
			boolean wordbased = false;
			

			ExportUtils.storeCustomTagMapForDoc(doc7, wordbased, idxs2, null, false);
			//export index
			DocxBuilder.writeDocxForDoc(doc7, false, true, false, new File("C:/Users/Schorsch/ArabicTest.docx"), idxs2, null, CustomTagFactory.getRegisteredTagNames(), false, false, false, false, true);
			//export with substitute abbreviations and preserve line breaks
//			DocxBuilder.writeDocxForDoc(doc5, true, true, true, new File("C:/Users/Administrator/DocxTest1.docx"), idxs2, null, CustomTagFactory.getRegisteredTagNames(), false, false, false, true, true);
//			//export with substitute abbreviations and not preserve line breaks
//			DocxBuilder.writeDocxForDoc(doc5, true, true, true, new File("C:/Users/Administrator/DocxTest2.docx"), idxs2, null, CustomTagFactory.getRegisteredTagNames(), false, false, false, true, false);
//			//export with expand abbreviations and preserve line breaks
//			DocxBuilder.writeDocxForDoc(doc5, true, true, true, new File("C:/Users/Administrator/DocxTest3.docx"), idxs2, null, CustomTagFactory.getRegisteredTagNames(), false, false, true, false, true);
//			//export with expand abbreviations and not preserve line breaks
//			DocxBuilder.writeDocxForDoc(doc5, true, true, true, new File("C:/Users/Administrator/DocxTest4.docx"), idxs2, null, CustomTagFactory.getRegisteredTagNames(), false, false, true, false, false);
//			//not preserve line breaks
//			DocxBuilder.writeDocxForDoc(doc5, true, true, true, new File("C:/Users/Administrator/DocxTest5.docx"), idxs2, null, CustomTagFactory.getRegisteredTagNames(), false, false, false, false, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
