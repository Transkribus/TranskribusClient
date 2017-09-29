package eu.transkribus.client.util;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

/**
 * Adds a field for a user-friendly error message to be displayed in error dialogs
 * 
 * @author philip
 *
 */
public class TrpServerErrorException extends ServerErrorException {
	private static final long serialVersionUID = -5027543549856917584L;
	
	private String messageToUser;
	
	public TrpServerErrorException(Status status) {
		super(status);
	}

	public TrpServerErrorException(int status) {
		super(status);
	}

	public TrpServerErrorException(Response response) {
		super(response);
	}

	public TrpServerErrorException(String message, Status status) {
		super(message, status);
	}

	public TrpServerErrorException(String message, int status) {
		super(message, status);
	}

	public TrpServerErrorException(String message, Response response) {
		super(message, response);
	}

	public TrpServerErrorException(Status status, Throwable cause) {
		super(status, cause);
	}

	public TrpServerErrorException(int status, Throwable cause) {
		super(status, cause);
	}

	public TrpServerErrorException(Response response, Throwable cause) {
		super(response, cause);
	}

	public TrpServerErrorException(String message, Status status, Throwable cause) {
		super(message, status, cause);
	}

	public TrpServerErrorException(String message, int status, Throwable cause) {
		super(message, status, cause);
	}

	public TrpServerErrorException(String message, Response response, Throwable cause) {
		super(message, response, cause);
	}

	// ↓ added constructors ↓
	
	public TrpServerErrorException(String message, String messageToUser, Status status) {
		this(message, status);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(String message, String messageToUser, int status) {
		this(message, status);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(String message, String messageToUser, Response response) {
		this(message, response);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(Status status, String messageToUser, Throwable cause) {
		this(status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(int status, String messageToUser, Throwable cause) {
		this(status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(Response response, String messageToUser, Throwable cause) {
		this(response, cause);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(String message, String messageToUser, Status status, Throwable cause) {
		this(message, status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(String message, String messageToUser, int status, Throwable cause) {
		this(message, status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpServerErrorException(String message, String messageToUser, Response response, Throwable cause) {
		this(message, response, cause);
		this.messageToUser = messageToUser;
	}
	
	public void setMessageToUser(String messageToUser) {
		this.messageToUser = messageToUser;
	}
	
	public String getMessageToUser() {
		if(StringUtils.isEmpty(messageToUser)) {
			return super.getMessage();
		}
		return messageToUser;
	}
}
