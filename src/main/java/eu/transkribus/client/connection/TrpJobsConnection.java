package eu.transkribus.client.connection;

import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.JerseyUtils;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.rest.RESTConst;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.core.util.xpath.XPathPage.TranscriptionLevel;

public class TrpJobsConnection extends ATrpServerConn {	
	private static final Logger logger = LoggerFactory.getLogger(TrpJobsConnection.class);
	
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
	
	public List<TrpJobStatus> getPendingJobs(List<String> tasks) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.GET_PENDING_JOBS_PATH);
		
		t = queryParam(t, RESTConst.TASKS_PARAM, CoreUtils.join(tasks));
		
		return getList(t, JOB_LIST_TYPE);
	}
	
	public boolean scheduleJob(String jobId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.SCHEDULE_JOB_PATH);
		
		t = queryParam(t, RESTConst.JOB_ID_PARAM, jobId);
		
		Response resp = t.request().post(null);
		
		checkStatus(resp, t);
		
		if (resp.getStatus() != 200) {
			logger.debug("scheduleJob, status = "+resp.getStatus()+ " - could not schedule job!");
			logger.debug(""+resp.getEntity());
			return false;
		} else {
			return true;
		}
	}
	
	public void updateJob(TrpJobStatus job) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.UPDATE_JOB_PATH);
		
		//FIXME is job as entity enough? HTR Training jobs exceed max. query param size
//		t = t.queryParam(RESTConst.JOB_PARAM, job);
		
		postEntity(t, job, MediaType.APPLICATION_XML_TYPE);
	}
	
	public TrpJobStatus queryJob(String jobId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.QUERY_JOB_PATH);
		t = t.queryParam(RESTConst.JOB_ID_PARAM, jobId);
		
		return postEntityReturnObject(t, null, MediaType.APPLICATION_XML_TYPE, TrpJobStatus.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public List<String> getModuleVersions(String name, boolean releaseOnly) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.GET_MODULE_VERSIONS_PATH);
		
		t = t.queryParam(RESTConst.NAME_PARAM, name);
		t = t.queryParam(RESTConst.RELEASE_ONLY_PARAM, releaseOnly);
		
		String json = getObject(t, String.class);
		return GsonUtil.toStrList2(json);		
	}
	
	public TrpTranscriptMetadata getCurrentTranscript(int pid) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.GET_TRANSCRIPT_PATH);
		t = t.queryParam(RESTConst.PAGE_ID_PARAM, pid);

		return getObject(t, TrpTranscriptMetadata.class);
	}
	
	public TrpTranscriptMetadata getTranscriptById(int tsid) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.GET_TRANSCRIPT_PATH);
		t = t.queryParam(RESTConst.TRANSCRIPT_ID_PARAM, tsid);

		return getObject(t, TrpTranscriptMetadata.class);
	}

	public TrpTranscriptMetadata updateTranscript(int pageId, EditStatus newStatus, int userId, String userName,
			PcGtsType page, String toolName, boolean overwrite, int parentId, String note) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.UPDATE_TRANSCRIPT_PATH);
		
		t = t.queryParam(RESTConst.PAGE_ID_PARAM, pageId);
		if (newStatus != null)
			t = t.queryParam(RESTConst.STATUS_PARAM, newStatus.toString());
		
		t = t.queryParam(RESTConst.USER_ID_PARAM, userId);
		t = queryParam(t, RESTConst.USER_PARAM, userName);
		t = queryParam(t, RESTConst.TOOL_NAME_PARAM, toolName);
		t = t.queryParam(RESTConst.OVERWRITE_PARAM, overwrite);
		t = t.queryParam(RESTConst.PARENT_ID_PARAM, parentId);
		t = queryParam(t, RESTConst.NOTE_PARAM, note);
		
		return postEntityReturnObject(t, page, MediaType.APPLICATION_XML_TYPE, TrpTranscriptMetadata.class, MediaType.APPLICATION_XML_TYPE);
	}

	public TrpPage getPageById(int pageId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.GET_PAGE_PATH);
		t = t.queryParam(RESTConst.PAGE_ID_PARAM, pageId);

		return getObject(t, TrpPage.class);
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
	
//	public List<TrpJobStatus> scheduleJobs(Set<Integer> jobIds) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
//		WebTarget t = baseTarget.path(RESTConst.JOB_MGMT_PATH).path(RESTConst.SCHEDULE_JOBS_PATH);
//
//		t = JerseyUtils.queryParam(t, RESTConst.JOB_IDS_PARAM, jobIds);
//
//		return getList(t, JOB_LIST_TYPE);
//	}
	
	

}
