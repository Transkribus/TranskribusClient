package eu.transkribus.client.catti;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.catti.CattiRequest;
import eu.transkribus.core.catti.CattiRequestDecoder;
import eu.transkribus.core.catti.CattiRequestEncoder;

@ClientEndpoint(decoders = CattiRequestDecoder.class, encoders = CattiRequestEncoder.class)
public class TrpCattiClientEndpoint {
	private final static Logger logger = LoggerFactory.getLogger(TrpCattiClientEndpoint.class);
	
//	public static final String DEFAULT_CATTI_URI = "ws://dbis-faxe.uibk.ac.at:8082/TrpCatti/";
	public static final String DEFAULT_CATTI_URI = "ws://dbis-faxe.uibk.ac.at:8080/TrpCatti/";
//	public static final String DEFAULT_CATTI_URI = "ws://localhost:8081/TrpCatti/";

	String baseUri = DEFAULT_CATTI_URI;
	int userid = 0;
	int docid = 1;
	int pid = 1;
	String lid = "whatever";

	String endpointURI;
	
	Session userSession = null;
    List<CattiMessageHandler> messageHandler = new ArrayList<>();
    
    public static interface CattiMessageHandler {
        public void handleMessage(CattiRequest message);
    }

	public TrpCattiClientEndpoint(String baseUri, int userid, int docid, int pid, String lid) throws Exception {
		if (baseUri != null)
			this.baseUri = baseUri;
		
		if (!this.baseUri.endsWith("/"))
			this.baseUri += "/";

		this.userid = userid;
		this.docid = docid;
		this.pid = pid;
		this.lid = lid;

//		endpointURI = this.baseUri + this.userid + "/" + this.docid + "/" + this.pid + "/" + this.lid;
		endpointURI = this.baseUri;
		
		logger.debug("endpoint url: "+endpointURI);

		// try {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		container.connectToServer(this, new URI(endpointURI));
		
		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }
	}
	
	public Session getUserSession() { 
		return userSession;
	}
	
	public void closeUserSession() {
		if (userSession != null) {
			try {
				userSession.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void sendObjectBasicRemote(Object o) throws IOException, EncodeException {
		if (userSession == null || userSession.getBasicRemote() == null) {
			logger.error("No active session while sending object: "+ ((o==null) ? "null" : o.toString()));
			return;
		}
		userSession.getBasicRemote().sendObject(o);
	}
	
	public boolean isOpen() {
		return userSession != null && userSession.isOpen();
	}

	@OnOpen public void onOpen(Session p) {
		logger.info("Opened session: " + p.getId());
		this.userSession = p;
	}

	@OnClose public void onClose(Session p) {
		logger.info("Closed session: " + p.getId());
		this.userSession = null;
	}

	@OnMessage public void onMessage(CattiRequest request) {
		for (CattiMessageHandler mh : messageHandler) {
			mh.handleMessage(request);
		}
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		logger.info("error: "+t.getMessage(), t);
	}
	
	public void addMessageHandler(CattiMessageHandler msgHandler) {
        messageHandler.add(msgHandler);
    }
	
	public void removeMessageHandler(CattiMessageHandler msgHandler) {
        messageHandler.remove(msgHandler);
    }	
	
	

}
