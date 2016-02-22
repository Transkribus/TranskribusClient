package eu.transkribus.client;

import java.util.List;
import java.util.concurrent.Future;

import javax.ws.rs.client.InvocationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDocMetadata;

public class DoSth {
	private final static Logger logger = LoggerFactory.getLogger(DoSth.class);
	
	public static void testAsyncClientCalls(final String user, final String pw) {
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.PROD_SERVER_URI, user, pw)) {
			
//			List<TrpDocMetadata> docs = conn.getAllDocsByUser(0, 0, null, null);
//			logger.info("got docs = "+docs.size());
			
			Future fut = conn.getAllDocsByUserAsync(0, 0, null, null, new InvocationCallback<List<TrpDocMetadata>>() {

				@Override public void completed(List<TrpDocMetadata> docs) {
					logger.info("SUCCCCESSSS");
					logger.info("response = "+docs);
				}

				@Override public void failed(Throwable throwable) {
					logger.info("ERRRROOORr");
					logger.error("error getting my docs: "+throwable.getMessage(), throwable);
				}
				
			});
			
			fut.get();
		
			logger.info("HERE");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void addDocsToCollectionForJoanReview2016(final String user, final String pw) {
		int[] docIds = new int[] { 
//				331, 332, 333, 334, 335, 336, 337, 338, 6,
//				31, 37, 101, 
//				84, 30, 40, 
//				41, 43, 45, 47, 48, 49, 50, 51, 52, 57, 58, 59, 
//				902, 
				
				
//				2444,
//				2867, 1034, 341, 1061, 
//				107, ?? 
//				936, 445 
				121
				};
		
		int colId = 1890;
		
		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.PROD_SERVER_URI, user, pw)) {		
			for (int id : docIds) {
				logger.info("adding document to collection: "+id);
				conn.addDocToCollection(colId, id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
//		addDocsToCollectionForJoanReview2016();
		testAsyncClientCalls(args[0], args[1]);
	}

}
