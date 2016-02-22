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

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.pdf.PdfExporter;
import eu.transkribus.core.model.builder.rtf.TrpRtfBuilder;

public class RtfTest {
	public static void main(String[] args) throws LoginException, MalformedURLException, DocumentException, IOException, JAXBException, URISyntaxException{
//		TrpServerConn conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[1], "philip", "test123");
//		TrpDoc doc = conn.getTrpDoc(69);
		
//		//TrpServerBentham - Batch2 - 363 pages
//		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], "philip", "test123");
//		TrpDoc doc = conn.getTrpDoc(606, 3230, -1);
//		
//		
//		TrpDoc doc4 = conn.getTrpDoc(940, 2867, -1);
//		
//		conn.close();
//			
//		@SuppressWarnings("serial")
//		Set<Integer> idxs2 = new HashSet<Integer>() {{
//			  add(2);
//			}};
//			
//		TrpRtfBuilder.writeRtfForDoc(doc4, false, false, false, new File("C:/Users/Administrator/RtfTest.rtf"), idxs2, null, CustomTagFactory.getRegisteredTagNames());
		
		//TrpRtfBuilder.writeRtfForDoc(doc4, false, new File("C:/Users/Administrator/KonzilsProtokolle_test2.rtf"), idxs2, null);
		//(new PdfExporter()).export(doc2, "C:/Users/Administrator/Reichsgericht_test.pdf", idxs2);

		//(new PdfExporter()).export(doc, "/tmp/doc107.pdf", null);
		
		//check params
		/*
		 * http://rosdok.uni-rostock.de/file/rosdok_document_0000007322/rosdok_derivate_0000026952/ppn778418405.dv.mets.xml
		 * 
		 * (https?|ftp)://(-\\.)?([^\\s/?\\.#-]+\\.?)+(/[^\\s]*)?$@iS
		 * #\b(([\w-]+://?|www[.])[^\s()<>]+(?:\([\w\d]+\)|([^[:punct:]\s]|/)))#iS
		 */
		
		String metsUrl = "http://rosdok.uni-rostock.de/file/rosdok_document_0000007322/rosdok_derivate_0000026952/ppn778418405.dv.mets.xml";
		Pattern p = Pattern.compile("(https?|ftp)://(-\\.)?([^\\s/?\\.#-]+\\.?)+(/[^\\s]*)?$@iS");
		Matcher m = p.matcher(metsUrl);//replace with string to compare
		if(m.find()) {
			System.out.println("String contains URL");
		}
		else{
			System.out.println("no URL?? " + metsUrl);
		}
		
		Matcher m2 = p.matcher("blablaTest");
		if(m2.find()){
			System.out.println("should not find this URL");
		}
		else{
			System.out.println("sURL matcher works");
		}
		
		try {
            URL url = new URL(metsUrl);
            // If possible then replace with anchor...
            System.out.println("valid URL found " + metsUrl);   
        } catch (MalformedURLException e) {
            // If there was an URL that was not it!...
        	System.out.println("MalformedURLException " + metsUrl);
        }

	}
}
