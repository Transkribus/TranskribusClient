package eu.transkribus.client.util;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.auth.TrpUserLogin;

public class SessionExpiredException extends LoginException implements IUserMessageException {
	private static final long serialVersionUID = -8207194647734517123L;
	private static final Logger logger = LoggerFactory.getLogger(SessionExpiredException.class);	
	private String messageToUser;
	
	public SessionExpiredException() {
	}

	public SessionExpiredException(String msg) {
		super(msg);
	}
	
	public SessionExpiredException(String msg, TrpUserLogin login) {
		super(msg + login!=null ? (" "+login.toString()) : "");
	}
	
	public SessionExpiredException(String msg, String messageToUser) {
		this(msg);
		this.messageToUser = messageToUser;
	}
	
	public SessionExpiredException(String msg, String messageToUser, TrpUserLogin login) {
		this(msg + login!=null ? (" "+login.toString()) : "");
		if(StringUtils.isEmpty(msg)) {
			logger.warn("A SessionExpiredException was created without message!");
		}
		this.messageToUser = messageToUser;
	}

	public String getMessageToUser() {
		if(StringUtils.isEmpty(messageToUser)) {
			return super.getMessage();
		}
		return messageToUser;
	}

	public void setMessageToUser(String messageToUser) {
		this.messageToUser = messageToUser;
	}

}
