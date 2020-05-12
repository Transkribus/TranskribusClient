package eu.transkribus.client.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.JerseyUtils;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.util.GsonUtil;

public class JobModuleConnection extends ATrpServerConn {
	private static final Logger logger = LoggerFactory.getLogger(JobModuleConnection.class);
	
	public JobModuleConnection(String url) throws LoginException {
		super(url);
	}
	
	public String getStringFromPath(String path) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = baseTarget.path(path);
		return getObject(t, String.class);		
	}
	
	public String getName() throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return getStringFromPath("name");
	}
	
	public String getVersion() throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return getStringFromPath("version");
	}
	
	public String getServer() throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return getStringFromPath("server");
	}	
	
	public String getUrl() throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return getStringFromPath("url");
	}
	
	public String getConf() throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return getStringFromPath("conf");
	}	
	
//	/**
//	 * @deprecated not working yet...
//	 */
	public List<String> getTasks() throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		// not working yet, since not all modules are
		try {
			return GsonUtil.toStrList(getStringFromPath("tasks"));
		} catch (Exception e) {
			logger.warn("Could not get tasks via direct path - maybe latest verison is not deployed - trying workaround...");
			// workaround: parse tasks from conf
			String conf = getConf();
			int i = conf.indexOf("tasks=");
			if (i == -1) {
				throw new TrpClientErrorException("Could not retrieve tasks from conf: "+conf, Status.BAD_REQUEST);
			}
			String tmp = conf.substring(i+"tasks=".length());
			tmp = tmp.substring(0, tmp.indexOf("]")+1);
			return GsonUtil.toStrList(tmp);
		}
	}
	
	public Map<String, List<String>> getRunningJobs() throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		String runningJobs = getStringFromPath("runningJobs");
		return GsonUtil.toMapWithStringListValues(runningJobs);
	}
	
	public String killJob(String jobId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
//		public static final String KILL_JOB_PATH = "killJob";
//		public static final String JOB_ID_PARAM = "jobId";
		
		WebTarget t = baseTarget.path("killJob");
		t = JerseyUtils.queryParam(t, "jobId", jobId);
		
		return postNullReturnObject(t, String.class);
	}

	public static void main(String[] args) throws LoginException {
//		String url = "http://dea-edison:8081/PyLaiaModule-trpProd-0.1.9";
		String url = "http://dea-bl03:8081/DummyModule-trpTest-0.0.9";
		JobModuleConnection c = new JobModuleConnection(url);
		
		System.out.println("name = "+c.getName());
		System.out.println("version = "+c.getVersion());
		System.out.println("server = "+c.getServer());
		System.out.println("url = "+c.getUrl());
		System.out.println("conf = "+c.getConf());
		System.out.println("tasks = "+c.getTasks());
		Map<String, List<String>> m = c.getRunningJobs();
		System.out.println("runningJobs = "+m);
//		System.out.println(m.get(m.keySet().iterator().next()));
		
//		try {
//			System.out.println(c.killJob(null));
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			System.out.println(c.killJob("irgendwas"));
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}		
		
		
		
	}

}
