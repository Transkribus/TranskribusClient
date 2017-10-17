package eu.transkribus.client.util;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

/**
 * Adds a field for a user-friendly error message to be displayed in error dialogs
 * 
 * @author philip
 *
 */
public class TrpClientErrorException extends ClientErrorException implements IUserMessageException {

	private static final long serialVersionUID = -8446093468998173143L;

	private String messageToUser;
	
	public TrpClientErrorException(Status status) {
		super(status);
	}

	public TrpClientErrorException(int status) {
		super(status);
	}

	public TrpClientErrorException(Response response) {
		super(response);
	}

	public TrpClientErrorException(String message, Status status) {
		super(message, status);
	}

	public TrpClientErrorException(String message, int status) {
		super(message, status);
	}

	public TrpClientErrorException(String message, Response response) {
		super(message, response);
	}

	public TrpClientErrorException(Status status, Throwable cause) {
		super(status, cause);
	}

	public TrpClientErrorException(int status, Throwable cause) {
		super(status, cause);
	}

	public TrpClientErrorException(Response response, Throwable cause) {
		super(response, cause);
	}

	public TrpClientErrorException(String message, Status status, Throwable cause) {
		super(message, status, cause);
	}

	public TrpClientErrorException(String message, int status, Throwable cause) {
		super(message, status, cause);
	}

	public TrpClientErrorException(String message, Response response, Throwable cause) {
		super(message, response, cause);
	}
	
	// ↓ added constructors ↓
	
	public TrpClientErrorException(String message, String messageToUser, Status status) {
		this(message, status);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(String message, String messageToUser, int status) {
		this(message, status);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(String message, String messageToUser, Response response) {
		this(message, response);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(Status status, String messageToUser, Throwable cause) {
		this(status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(int status, String messageToUser, Throwable cause) {
		this(status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(Response response, String messageToUser, Throwable cause) {
		this(response, cause);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(String message, String messageToUser, Status status, Throwable cause) {
		this(message, status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(String message, String messageToUser, int status, Throwable cause) {
		this(message, status, cause);
		this.messageToUser = messageToUser;
	}

	public TrpClientErrorException(String message, String messageToUser, Response response, Throwable cause) {
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
