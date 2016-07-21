package eu.transkribus.client.connection;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ServerErrorException;

import eu.transkribus.core.model.beans.job.TrpJobStatus;

public class BatchLaTest {
	
	final static int colId = 2;
	final static int docId = 1035; //62
	
	public static void main(String[] args){
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], args[0], args[1])) {
			
			final String jobId = conn.analyzeLayoutBatch(colId, docId, "1-5", true, true);
			
			try {
				while(true){
					TrpJobStatus job = conn.getJob(jobId);
					System.out.println(job.toString());
					if(job.getEndTime() > 0){
						break;
					}
					Thread.sleep(3000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (LoginException e){
			e.printStackTrace();
		} catch (ServerErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
