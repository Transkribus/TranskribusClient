package eu.transkribus.client.connection;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;

public class DuplicateDocumentTest {
private static final Logger logger = LoggerFactory.getLogger(TrpServerConnTest.class);
	
	public static void main(String[] args) throws Exception {
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
		final int docId = 62;
		final int colId = 2;
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], args[0], args[1])) {
			logger.info(conn.duplicateDocument(colId, docId, "Test copy of Bentham Box 35", null));
		} catch (LoginException e){
			e.printStackTrace();
		}
	}
}
