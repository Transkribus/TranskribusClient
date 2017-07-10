package eu.transkribus.client;

import java.util.ArrayList;
import java.util.List;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.builder.CommonExportPars;
import eu.transkribus.core.model.builder.alto.AltoExportPars;
import eu.transkribus.core.model.builder.docx.DocxExportPars;
import eu.transkribus.core.model.builder.pdf.PdfExportPars;
import eu.transkribus.core.model.builder.tei.TeiExportPars;

public class SebisTestClass {

	private static List<DocumentSelectionDescriptor> getAllCollectionDocsExcept(TrpServerConn conn, int colId, int ...docIdExceptions) throws Exception {
		List<DocumentSelectionDescriptor> dsds = new ArrayList<>();
		List<TrpDocMetadata> docs = conn.getAllDocs(colId);
		System.out.println("nr of total docs: "+docs.size());
		for (TrpDocMetadata md : docs) {
			boolean skip = false;
			for (int notThisDocId : docIdExceptions) {
				if (md.getDocId() == notThisDocId) {
					skip = true;
					break;
				}
			}
			if (!skip) {
				dsds.add(new DocumentSelectionDescriptor(md.getDocId()));
			}
		}
		
		return dsds;
	}

	static void startServerExport(final String user, final String pw, boolean prodServer, boolean dryRun) throws Exception {
		try (TrpServerConn conn = new TrpServerConn(prodServer ? TrpServerConn.SERVER_URIS[0] : TrpServerConn.SERVER_URIS[1], user, pw)) {
//			conn.exportDocument(2, 1312, "1-31", true, true, true, true, false, true, true, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false, false, false, true, false, false, false, "Latest");
			
			CommonExportPars commonPars = new CommonExportPars();
//			commonPars.setDoWriteTei(true);
//			commonPars.setDoWritePdf(true);
//			commonPars.setDoWriteDocx(true);
//			commonPars.setDoWriteTagsXlsx(true);
//			commonPars.setDoWriteTablesXlsx(true);
			
			AltoExportPars altoPars = new AltoExportPars();
			PdfExportPars pdfPars = new PdfExportPars();
			TeiExportPars teiPars = new TeiExportPars();
			DocxExportPars docxPars = new DocxExportPars();
			
//			String jobID = conn.exportDocument(2, 1312, commonPars, altoPars, pdfPars, teiPars, docxPars);
//			System.out.println("Started export job "+jobID);
			
			
			int collId = 2408;
			List<DocumentSelectionDescriptor> dsds = getAllCollectionDocsExcept(conn, collId, 13385);
			System.out.println("nr of docs to export: "+dsds.size());
			
			if (!dryRun) {
				String jobID = conn.exportDocuments(collId, dsds, commonPars, altoPars, pdfPars, teiPars, docxPars);
				System.out.println("Started export job "+jobID);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		startServerExport(args[0], args[1], true, true);
	}

}
