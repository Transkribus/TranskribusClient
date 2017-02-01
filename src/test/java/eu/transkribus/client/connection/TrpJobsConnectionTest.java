package eu.transkribus.client.connection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.TrpJobStatus;

public class TrpJobsConnectionTest {
	private final static Logger logger = LoggerFactory.getLogger(TrpJobsConnectionTest.class);
	
//	public static void testRegister() throws Exception {
//		TrpJobsConnection conn = TrpJobsConnection.create(false);
//		
//		String jobType = "my_jobType";
//		String jobTask = "my_jobTask";
//		String toolProvider = "my_toolProvider";
//		String toolVersion = "my_toolVersion";
//		String url = "my_url";
//		boolean register=true;
//		
//		// register:
//		conn.registerModule(url, jobType, jobTask, toolProvider, toolVersion, register);
//		
//		// unregister:
////		conn.registerModule(url, null, null, null, null, !register);
//		
//	}
	
//	public static void testRetrieveAndSchedule() throws Exception {
//		TrpJobsConnection conn = TrpJobsConnection.create(false);
//		
//		String jobType = "";
//		String jobTask = "";
//		String toolProvider = "";
//		String toolVersion = "";
//		String host = "";
//		
//		List<TrpJobStatus> jobsRetrieved = conn.retrieveJobs(jobType, jobTask, toolProvider, toolVersion, host);
//		
//		System.out.println("N retrieved jobs: "+jobsRetrieved.size());
//		jobsRetrieved.stream().forEach((j) -> {
//			System.out.println(j.toString());
//		});
//				
//		Set<Integer> jobIds = new HashSet<>();
//		jobsRetrieved.stream().forEach((j) -> {
//			try {
//				Integer ji = Integer.parseInt(j.getJobId());				
//				jobIds.add(ji);
//			} catch (NumberFormatException e) {
//				logger.error("Cannot parse jobId: "+j.getJobId(), e);
//			}
//		});
//		
//		List<TrpJobStatus> jobsScheduled = conn.scheduleJobs(jobIds);
//		
//		System.out.println("N scheduled jobs: "+jobsScheduled.size());
//		jobsScheduled.stream().forEach((j) -> {
//			System.out.println(j.toString());
//		});
//		
//	}

	public static void main(String[] args) throws Exception {
		
//		testRetrieveAndSchedule();
//		testRegister();
	}

}
