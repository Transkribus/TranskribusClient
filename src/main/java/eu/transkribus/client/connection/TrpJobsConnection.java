package eu.transkribus.client.connection;

import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import eu.transkribus.client.util.JerseyUtils;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.rest.RESTConst;

public class TrpJobsConnection extends ATrpServerConn {	
	
	public static TrpJobsConnection create(boolean prodServer) throws LoginException {
		if (prodServer) {
			return new TrpJobsConnection(PROD_SERVER_URI);
		}
		else {
			return new TrpJobsConnection(TEST_SERVER_URI);
		}
	}

	public TrpJobsConnection(String uriStr) throws LoginException {
		super(uriStr);
	}
	
	public void registerModule(String url, String name, String tasks, String version,
			boolean register) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.REGISTER_JOB_MODULE_PATH);
		
		t = queryParam(t, RESTConst.URL_PARAM, url);
		t = queryParam(t, RESTConst.NAME_PARAM, name);
		t = queryParam(t, RESTConst.TASKS_PARAM, tasks);
		t = queryParam(t, RESTConst.VERSION_PARAM, version);
		t = t.queryParam(RESTConst.REGISTER_PARAM, register);
		
		postNull(t);
	}
	
//	public List<TrpJobStatus> retrieveJobs(String jobType, String jobTask, String toolProvider, String toolVersion, String host) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
//		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.RETRIEVE_JOBS_PATH);
//
//		t = JerseyUtils.queryParam(t, RESTConst.JOB_TYPE_PARAM, jobType);
//		t = JerseyUtils.queryParam(t, RESTConst.JOB_TASK_PARAM, jobTask);
//		t = JerseyUtils.queryParam(t, RESTConst.TOOL_PROVIDER_PARAM, toolProvider);
//		t = JerseyUtils.queryParam(t, RESTConst.TOOL_VERSION_PARAM, toolVersion);
//		t = JerseyUtils.queryParam(t, RESTConst.TOOL_HOST_PARAM, host);
//
//		return getList(t, JOB_LIST_TYPE);
//	}
	
	public List<TrpJobStatus> scheduleJobs(Set<Integer> jobIds) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.SCHEDULE_JOBS_PATH);

		t = JerseyUtils.queryParam(t, RESTConst.JOB_IDS_PARAM, jobIds);

		return getList(t, JOB_LIST_TYPE);
	}
	
	

}
