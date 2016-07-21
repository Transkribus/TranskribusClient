package eu.transkribus.client.tools;

import javax.security.auth.login.LoginException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;

public class StatusUpdater {
	private static final Logger logger = LoggerFactory.getLogger(StatusUpdater.class);
	public static void main(String[] args) throws LoginException, JAXBException{
		
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
		final int colId = 2247;
		final int[] docIds = {5010};
		
		TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], args[0], args[1]);
		
		for(int docId : docIds){
			TrpDoc doc = conn.getTrpDoc(colId, docId, 1);
			for(TrpPage p : doc.getPages()){
				final int pageNr = p.getPageNr();
				logger.debug("In page: " + pageNr);
				if(pageNr < 60){
//				if(!EditStatus.DONE.equals(p.getCurrentTranscript().getStatus())) {
					TrpTranscriptMetadata newMd = conn.updateTranscript(colId, docId, pageNr, EditStatus.DONE, null, -1, null);
				}
			}
		}
	}
}
