package eu.transkribus.client.catti;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.catti.TrpCattiClientEndpoint.CattiMessageHandler;
import eu.transkribus.core.catti.CattiMethod;
import eu.transkribus.core.catti.CattiRequest;

public class CattiClientTest {
	private final static Logger logger = LoggerFactory.getLogger(CattiClientTest.class);
	
    public static void main(String [] args) {
        try {
        	String baseUri = "ws://dbis-faxe.uibk.ac.at:8082/TrpCatti";
//        	String baseUri = "ws://localhost:8081/TrpCatti";
        	int userid = 0;
        	int docid = 62;
        	int pid = 1;
        	String lid = "r2l1";
        	
        	final CountDownLatch messageLatch = new CountDownLatch(1);

//        	CattiRequest r = new CattiRequest(userid, docid, pid, lid, CattiMethod.SET_PREFIX, "pref", "suff", false, "");

        	TrpCattiClientEndpoint ce = new TrpCattiClientEndpoint(baseUri, userid, docid, pid, lid);
        	ce.addMessageHandler(new CattiMessageHandler() {
				@Override public void handleMessage(CattiRequest request) {
					
					if (!request.hasError()) {
						logger.info("recieved catti request: "+request);	
						
					} else {
						logger.error("Error in request: "+request.getError());
					}
					
//					messageLatch.countDown();
				}
			});
        	
        	ce.closeUserSession(); // TEST
        	
        	CattiRequest r = new CattiRequest(userid, docid, pid, lid, CattiMethod.SET_PREFIX, "", "", false, "");
        	ce.getUserSession().getBasicRemote().sendObject(r);
        	
        	
        	
        	r = new CattiRequest(userid, docid, pid, lid, CattiMethod.REJECT_SUFFIX, "is that", "of", false, "");
        	ce.getUserSession().getBasicRemote().sendObject(r);
        	
        	Thread.currentThread().sleep(3000);
        	
        	ce.closeUserSession();
//        	
//        	r = new CattiRequest(userid, docid, pid, lid, CattiMethod.REJECT_SUFFIX, "", "is that of the recapitulatory examination", false, "");
//        	ce.getUserSession().getBasicRemote().sendObject(r);

            messageLatch.await(100, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
