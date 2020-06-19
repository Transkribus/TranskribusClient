package eu.transkribus.client.connection;

import javax.ws.rs.client.WebTarget;

public abstract class ApiResourcePath {
	
	protected ATrpServerConn conn;
	private final String basePath;
	
	ApiResourcePath (ATrpServerConn conn) {
		this(conn, null);
	}
	
	ApiResourcePath (ATrpServerConn conn, final String basePath) {
		this.conn = conn;
		this.basePath = basePath;
	}
	
	protected WebTarget getBaseTarget() {
		if(basePath == null) {
			return conn.baseTarget;
		}
		return conn.baseTarget.path(basePath);
	}
}
