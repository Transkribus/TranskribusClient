package eu.transkribus.client.util;

import java.util.TimerTask;

import javax.ws.rs.ServerErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;

public class KeepSessionAliveTask extends TimerTask {
	private final static Logger logger = LoggerFactory.getLogger(KeepSessionAliveTask.class);
	
	TrpServerConn conn;
	public Exception e = null;

	public KeepSessionAliveTask(TrpServerConn conn) {
		this.conn = conn;
	}

	@Override public void run() {
		if (e == null) {
			try {
				logger.debug("refreshing session...");
				conn.refreshSession();
			} catch (SessionExpiredException | ServerErrorException e) {
				this.e = e;
				onException(e);
				logger.error(e.getMessage(), e);
			}
		} else {
			logger.debug("not refreshing anymore since error has been thrown: "+e.getMessage());
		}
	}
	
	public void onException(Exception e) {
	}

}
