package eu.transkribus.client.connection;

import javax.security.auth.login.LoginException;

import eu.transkribus.core.model.beans.enums.SearchType;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.util.SebisStopWatch;

public class SolrTest {
	
	public static void main(String[] args) throws LoginException{
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
//		TrpServerConn conn = null;

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], args[0], args[1])) {
			SebisStopWatch s = new SebisStopWatch();
			s.start();
			FulltextSearchResult f = conn.searchFulltext("P*", SearchType.Lines, 0, 10, null);
			s.stop();
			System.out.println(f.getNumResults());
			System.out.println(f);
			
			System.out.println(s.getTimeStr());
			
		}
//			
	}
}
