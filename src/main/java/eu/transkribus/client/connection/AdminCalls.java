package eu.transkribus.client.connection;

import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.rest.RESTConst;
import eu.transkribus.core.util.GsonUtil;

public class AdminCalls {
	private static final Logger logger = LoggerFactory.getLogger(AdminCalls.class);
	
	ATrpServerConn conn;
	
	public AdminCalls(ATrpServerConn conn) {
		this.conn = conn;
	}
	
	private WebTarget getBaseTarget() {
		return conn.baseTarget.path(RESTConst.ADMIN_PATH);
	}
	
	public String allowUsersForJob(List<String> userList, String jobImpl) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = getBaseTarget();
		t = t.path(RESTConst.USER_PATH).path(RESTConst.AUTH_PATH).path(jobImpl);
		
		String userListStr = GsonUtil.toJson(userList);
		logger.debug("userListStr = "+userListStr);
		
//		conn.postEntity(t, model, MediaType.APPLICATION_JSON_TYPE);
		return conn.postEntityReturnObject(t, userListStr, MediaType.APPLICATION_JSON_TYPE, String.class, MediaType.TEXT_PLAIN_TYPE);
	}
	
	public List<String> getUserNamesForJobImpl(String jobImpl) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = getBaseTarget();
		t = t.path(RESTConst.USER_PATH).path(RESTConst.AUTH_PATH).path(jobImpl).path(RESTConst.LIST_PATH);
		
		String responseStr = conn.getObject(t, String.class, MediaType.APPLICATION_JSON_TYPE);
		return GsonUtil.toStrList(responseStr);
	}
	

}
