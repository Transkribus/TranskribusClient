package eu.transkribus.client.util;

import javax.security.auth.login.LoginException;

import eu.transkribus.core.model.beans.auth.TrpUserLogin;

public class SessionExpiredException extends LoginException {
	private static final long serialVersionUID = -8207194647734517123L;

	public SessionExpiredException() {
	}

	public SessionExpiredException(String msg) {
		super(msg);
	}
	
	public SessionExpiredException(String msg, TrpUserLogin login) {
		super(msg + login!=null ? (" "+login.toString()) : "");
	}

}
