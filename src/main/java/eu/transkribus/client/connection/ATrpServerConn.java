package eu.transkribus.client.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.ClientRequestAuthFilter2;
import eu.transkribus.client.util.JulFacade;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.ClientVersionNotSupportedException;
import eu.transkribus.core.exceptions.OAuthTokenRevokedException;
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.PageLock;
import eu.transkribus.core.model.beans.TestBean;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpEvent;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.TrpWordgraph;
import eu.transkribus.core.model.beans.adapters.MetsMessageBodyWriter;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.rest.RESTConst;

/**
 * Abstract TRP Server Connection class that encapsulates the Jersey Client boilerplate.
 * 
 * @author philip
 *
 */
public abstract class ATrpServerConn implements Closeable {
	private final static Logger logger = LoggerFactory.getLogger(ATrpServerConn.class);
	
	/**
	 * Set DEBUG=true if you want this Connection to log all requests/responses in detail
	 */
	public final static boolean DEBUG = false;
	
	public static final GenericType<List<TrpTranscriptMetadata>> TRANS_MD_LIST_TYPE = new GenericType<List<TrpTranscriptMetadata>>() {};
	public static final GenericType<List<TrpCollection>> COL_LIST_TYPE = new GenericType<List<TrpCollection>>() {};
	public static final GenericType<List<TrpDocMetadata>> DOC_MD_LIST_TYPE = new GenericType<List<TrpDocMetadata>>() {};
	public static final GenericType<List<TrpJobStatus>> JOB_LIST_TYPE = new GenericType<List<TrpJobStatus>>() {};
	public static final GenericType<List<TrpWordgraph>> WORDGRAPH_LIST_TYPE = new GenericType<List<TrpWordgraph>>() {};
	public static final GenericType<List<PageLock>> PAGELOCK_LIST_TYPE = new GenericType<List<PageLock>>() {};
	public static final GenericType<List<EdFeature>> ED_FEATURE_LIST_TYPE = new GenericType<List<EdFeature>>() {};
	public static final GenericType<List<TrpDocDir>> DOC_DIR_LIST_TYPE = new GenericType<List<TrpDocDir>>() {};
	public static final GenericType<List<TrpEvent>> EVENT_LIST_TYPE = new GenericType<List<TrpEvent>>() {};
	public static final GenericType<List<TrpDbTag>> DB_TAG_LIST_TYPE = new GenericType<List<TrpDbTag>>() {};
	public static final GenericType<List<TestBean>> TEST_BEAN_LIST_TYPE = new GenericType<List<TestBean>>() {};
	public static final GenericType<List<String>> STRING_LIST_TYPE = new GenericType<List<String>>() {};
	
	public static final String PROD_SERVER_URI = "https://transkribus.eu/TrpServer";
	public static final String TEST_SERVER_URI = "https://transkribus.eu/TrpServerTesting";
	public static final String OLD_TEST_SERVER_URI = "https://dbis-faxe.uibk.ac.at/TrpServerTesting";
		
	public static final String[] SERVER_URIS = new String[] {
		PROD_SERVER_URI, 
		TEST_SERVER_URI,
		OLD_TEST_SERVER_URI
	};	
	
	public enum TrpServer {
		Prod(PROD_SERVER_URI),
		Test(TEST_SERVER_URI),
		OldTest(OLD_TEST_SERVER_URI);
		private final String uri;
		private TrpServer(String uri) {
			this.uri = uri;
		}
		public String getUriStr() {
			return uri;
		}
	}
	
	public static final int DEFAULT_URI_INDEX = 0;
	
	private /*static*/ Client client;
	private /*static*/ TrpUserLogin login;
	private /*static*/ URI serverUri;
	protected /*static*/ WebTarget baseTarget;
	private /*static*/ WebTarget loginTarget;
	private /*static*/ WebTarget loginOAuthTarget;
	
	protected final static MediaType DEFAULT_RESP_TYPE = MediaType.APPLICATION_JSON_TYPE;
	
	public static String guiVersion="NA";
	public static final int clientId = 1;
	
	public final static String DEFAULT_URI_ENCODING = "UTF-8";
	
	protected ATrpServerConn(final String uriStr) throws LoginException {
		if (StringUtils.isEmpty(uriStr)) {
			throw new LoginException("Server URI is not set!");
		}
		serverUri = UriBuilder.fromUri(uriStr).build();
		
		//FIXME it seems like there is some internal buffering.
		// how to get to this property?
		//httpUrlConnection.setChunkedStreamingMode(chunklength) - disables buffering and uses chunked transfer encoding to send request
		ClientConfig config = new ClientConfig();
		HttpUrlConnectorProvider prov = new HttpUrlConnectorProvider();
		prov.chunkSize(1024);
//		logger.debug("USE_FIXED_STREAMING_LENGTH = " + prov.USE_FIXED_LENGTH_STREAMING.toString());
		config.connectorProvider(prov);
		config.register(MultiPartFeature.class);
				
		client = ClientBuilder.newClient(config);
		client.register(new ClientRequestAuthFilter2(this));
		client.register(new ClientRequestFilter() {
			@Override public void filter(ClientRequestContext requestContext) throws IOException {
				List<Object> vers = new ArrayList<>(1);
				vers.add(ATrpServerConn.guiVersion);
				List<Object> clientIdList = new ArrayList<>(1);
				clientIdList.add(clientId);
				requestContext.getHeaders().put(RESTConst.GUI_VERSION_HEADER_KEY, vers);
				requestContext.getHeaders().put(RESTConst.CLIENT_ID_HEADER_KEY, clientIdList);
			}
		});
		
		//TrpDocUploadHttp might send METS
		client.register(new MetsMessageBodyWriter());
		
		enableDebugLogging(DEBUG);
		
		initTargets();
//		initBaseTarget();	

//		//register auth filter with the jSessionId and update the WebTarget accordingly
//		client.register(new ClientRequestAuthFilter(login.getSessionId()));
	}
			
	/**
	 * When parameter is true, a LoggingFilter will be registered with the client.<br/>
	 * Each request and response will then be logged in detail.<br/>
	 * TODO If Jersey is updated, LoggingFilter has to be replaced with LoggingFeature
	 * @param enableDebugLog
	 */
	public void enableDebugLogging(boolean enableDebugLog) {
		if(enableDebugLog) {
			LoggingFilter lf = new LoggingFilter(new JulFacade(logger), true);
			client.register(lf);
		}
	}

	protected boolean isSameServer(final String uriStr) {
		URI serverUriStr = UriBuilder.fromUri(uriStr).build();
		return serverUriStr.equals(serverUri);		
	}
	
//	protected ATrpServerConn(final String uriStr, final String user, final String pw) throws LoginException {
//		this(uriStr);
//		
//		if (user == null || user.isEmpty() || pw == null || pw.isEmpty()) {
//			throw new LoginException("Credentials must not be empty!");
//		}
//		
//		// LOGIN STUFF:
//		//authenticate and retrieve the session data
//		login = login(user, pw);
//		
//		//register auth filter with the jSessionId and update the WebTarget accordingly
//		client.register(new ClientRequestAuthFilter(login.getSessionId()));
//		initBaseTarget();
//	}
	
	private void initTargets() {
		loginTarget = client.target(serverUri).path(RESTConst.BASE_PATH).path(RESTConst.AUTH_PATH).path(RESTConst.LOGIN_PATH);
		loginOAuthTarget = client.target(serverUri).path(RESTConst.BASE_PATH).path(RESTConst.AUTH_PATH).path(RESTConst.LOGIN_OAUTH_PATH);
		baseTarget = client.target(serverUri).path(RESTConst.BASE_PATH);
		baseTarget.register(GZipEncoder.class);
	}
	
	// OLD: 
//	protected ATrpServerConn(final String uriStr, final String user, final String pw) throws LoginException {
//		if (user == null || user.isEmpty() || pw == null || pw.isEmpty()) {
//			throw new LoginException("Credentials must not be empty!");
//		}
//		if (uriStr == null || uriStr.isEmpty()) {
//			throw new LoginException("Server URI is not set!");
//		}
//		serverUri = UriBuilder.fromUri(uriStr).build();
//		
//		//FIXME it seems like there is some internal buffering.
//		// how to get to this property?
//		//httpUrlConnection.setChunkedStreamingMode(chunklength) - disables buffering and uses chunked transfer encoding to send request
//		ClientConfig config = new ClientConfig();
//		HttpUrlConnectorProvider prov = new HttpUrlConnectorProvider();
//		prov.chunkSize(1024);
////		logger.debug("USE_FIXED_STREAMING_LENGTH = " + prov.USE_FIXED_LENGTH_STREAMING.toString());
//		config.connectorProvider(prov);
//		config.register(MultiPartFeature.class);
//		client = ClientBuilder.newClient(config);
//		loginTarget = client.target(serverUri).path(RESTConst.BASE_PATH).path(RESTConst.AUTH_PATH).path(RESTConst.LOGIN_PATH);
//		//authenticate and retrieve the session data
//		login = login(user, pw);
//		//register auth filter with the jSessionId and update the WebTarget accordingly
//		client.register(new ClientRequestAuthFilter(login.getSessionId()));
//		baseTarget = client.target(serverUri).path(RESTConst.BASE_PATH);
//		//TODO test this:
//		baseTarget.register(GZipEncoder.class);
//	}
	// END OLD
		
	@Override
	public void close() {
		logout();
		client.close();
	}

	// called upon garbage collection:
	@Override protected void finalize() throws Throwable {
		close();
	};

	public TrpUserLogin login(final String user, final String pw) throws ClientVersionNotSupportedException, LoginException {
		if (login != null) {
			logout();
		}

		//post creds to /rest/auth/login
		Form form = new Form();
		form = form.param(RESTConst.USER_PARAM, user);
		form = form.param(RESTConst.PW_PARAM, pw);
		
		login = null;
		try {
			login = postEntityReturnObject(
					loginTarget, 
					form, MediaType.APPLICATION_FORM_URLENCODED_TYPE, 
					TrpUserLogin.class, MediaType.APPLICATION_JSON_TYPE
					);
			
			initTargets();
		}
		catch (ClientErrorException e) {
//			throw e;

//			String entity = readStringEntity(e.getResponse());
			
			login = null;
			if(e.getResponse().getStatus() == ClientVersionNotSupportedException.STATUS_CODE) {
				logger.debug("ClientVersionNotSupportedException on login!");
				throw new ClientVersionNotSupportedException(e.getMessage());
			} else {
				throw new LoginException(e.getMessage());
			}
		}
		catch(Exception e) {
			login = null;
			logger.error("Login request failed!", e);
			throw new LoginException(e.getMessage());
		}
		logger.debug("Logged in as: " + login.toString());
		
		return login;
	}
	
	public TrpUserLogin loginOAuth(final String code, final String state, final String grantType, final String redirectUri, final OAuthProvider prov) throws LoginException, OAuthTokenRevokedException {
		if (login != null) {
			logout();
		}

		//post creds to /rest/auth/login
		Form form = new Form();
		form = form.param(RESTConst.CODE_PARAM, code);
		form = form.param(RESTConst.STATE_PARAM, state);
		form = form.param(RESTConst.TYPE_PARAM, grantType);
		form = form.param(RESTConst.PROVIDER_PARAM, prov.toString());
		form = form.param(RESTConst.REDIRECT_URI_PARAM, redirectUri);
		
		login = null;
		try {
			login = postEntityReturnObject(
					loginOAuthTarget, 
					form, MediaType.APPLICATION_FORM_URLENCODED_TYPE, 
					TrpUserLogin.class, MediaType.APPLICATION_JSON_TYPE
					);
			
			initTargets();
		} catch(ClientErrorException cee){
			if(cee.getResponse().getStatus() == 403) {
				login = null;
				throw new OAuthTokenRevokedException();				
			} else {
				login = null;
				logger.error("Login request failed!", cee);
				throw new LoginException(cee.getMessage());
			}
		} catch(Exception e) {
			login = null;
			logger.error("Login request failed!", e);
			throw new LoginException(e.getMessage());
		}
		logger.debug("Logged in as: " + login.toString());
		
		return login;
	}
	
	public static WebTarget queryParam(WebTarget t, String param, String value) {
		if (t != null && !StringUtils.isEmpty(param) && !StringUtils.isEmpty((String)value)) {
			return t.queryParam(param, value);
		}
		return t;
	}
	
	public static WebTarget queryParam(WebTarget t, String param, Iterable<?> value) {
		if (t != null && !StringUtils.isEmpty(param) && value!=null) {
			return t.queryParam(param, value);
		}
		return t;
	}

	public void logout() throws ServerErrorException, ClientErrorException {
		try {
			final WebTarget target = baseTarget.path(RESTConst.AUTH_PATH).path(RESTConst.LOGOUT_PATH);
			//just post a null entity. SessionId is added in RequestAuthFilter
			Response resp = target.request().post(null);
			checkStatus(resp, target);
		} catch(SessionExpiredException see) {
			logger.info("Logout failed as session has expired or sessionId is invalid.");
		} finally {
			login = null;
//			client.close();
		}
	}
	
	public TrpUserLogin getUserLogin() {
		return login;
	}
	
	public String getServerUri() {
		return serverUri.toString();
	}
	
	protected <T> List<T> getList(WebTarget target, GenericType<List<T>> returnType) throws SessionExpiredException, ServerErrorException, ClientErrorException{
		return getList(target, returnType, DEFAULT_RESP_TYPE);
	}
	
	protected <T> List<T> getList(WebTarget target, GenericType<List<T>> returnType, MediaType responseType) throws SessionExpiredException, ServerErrorException, ClientErrorException{
		if (responseType == null)
			responseType = DEFAULT_RESP_TYPE;
		
//		SebisStopWatch.SW.start();
		Response resp = target.request(responseType).get();
//		SebisStopWatch.SW.stop(true, "time for request ");
		
//		SebisStopWatch.SW.start();
		checkStatus(resp, target);
//		SebisStopWatch.SW.stop(true, "time for checking status ");
		
//		SebisStopWatch.SW.start();
//		resp.bufferEntity();
		final List<T> genericList = extractList(resp, returnType);
//		SebisStopWatch.SW.stop(true, "time for extracting list ");
		return genericList;
	}
		
//	protected <T> List<T> getListAsync(WebTarget target, GenericType<List<T>> returnType, InvocationCallback<Response> callback) throws SessionExpiredException, ServerErrorException, ClientErrorException{
//		Future<Response> fut = target.request(DEFAULT_RESP_TYPE).async().get(callback);
//		
//		checkStatus(resp, target);
//		final List<T> genericList = extractList(resp, returnType);
//		return genericList;
//	}	
	
	protected <T> T getObject(WebTarget target, Class<T> clazz) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		return getObject(target, clazz, null);
	}
	
	protected <T> T getObject(WebTarget target, Class<T> clazz, MediaType type) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(type == null){
			type = DEFAULT_RESP_TYPE;
		}
		Response resp = target.request(type).get();
		checkStatus(resp, target);
		T object = extractObject(resp, clazz);
		return object;
	}
	
	protected void delete(WebTarget target) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		Response resp = target.request().delete();
		checkStatus(resp, target);
	}
	
	protected <T> void postEntity(WebTarget target, T entity, MediaType postMediaType) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		Response resp = target.request().post(Entity.entity(entity, postMediaType));
		checkStatus(resp, target);
	}
	
	protected <T> void postNull(WebTarget target) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		Response resp = target.request().post(null);
		checkStatus(resp, target);
	}
	
	protected <T, R> R postNullReturnObject(WebTarget target, Class<R> returnType) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		Response resp = target.request().post(null);
		checkStatus(resp, target);
		return extractObject(resp, returnType);
	}
	
	protected <T, R> R postNullReturnObject(WebTarget target, T entity, MediaType postMediaType, Class<R> returnType) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		Response resp = target.request().post(Entity.entity(entity, postMediaType));
		checkStatus(resp, target);
		return extractObject(resp, returnType);
	}
	
	protected <T, R> R postXmlEntityReturnObject(WebTarget target, T entity, Class<R> returnType) throws SessionExpiredException, ServerErrorException, ClientErrorException{
		return postEntityReturnObject(target, entity, MediaType.APPLICATION_XML_TYPE, returnType, DEFAULT_RESP_TYPE);
	}
	
	protected <T, R> R postEntityReturnObject(WebTarget target, T entity, MediaType postMediaType, Class<R> returnType, MediaType returnMediaType) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		Entity<T> ent = buildEntity(entity, postMediaType);
		Response resp = target.request(returnMediaType).post(ent);
		checkStatus(resp, target);
		R object = extractObject(resp, returnType);
		return object;
	}
	
	public <T, R> R putEntityReturnObject(WebTarget target, T entity, MediaType postMediaType, Class<R> returnType, MediaType returnMediaType) throws SessionExpiredException, ClientErrorException, ServerErrorException {
		Entity<T> ent = buildEntity(entity, postMediaType);
		Response resp = target.request(returnMediaType).put(ent);
		checkStatus(resp, target);
		R object = extractObject(resp, returnType);
		return object;		
	}
	
	/**
	 * Does not work with Tomcat 7 (yet). See server.rest.Layout.java
	 */
	//	protected <T, R> R asyncPostEntityReturnObject(WebTarget target, T entity, MediaType postMediaType, Class<R> returnType, MediaType returnMediaType) throws InterruptedException, ExecutionException, SessionExpiredException, ServerErrorException, ClientErrorException{
	//		final Entity<T> ent = buildEntity(entity, postMediaType);
	//		final AsyncInvoker asyncInvoker = target.request(returnMediaType).async();
	//		final Future<Response> responseFuture = asyncInvoker.post(ent);
	//		logger.debug("Request is being processed asynchronously.");
	//		final Response resp = responseFuture.get();
	//		// get() waits for the response to be ready
	//		checkStatus(resp, target);
	//		R object = extractObject(resp, returnType);
	//		logger.debug("Response received.");
	//		return object;
	//	}
	
	protected <T, R> List<R> postXmlEntityReturnList(WebTarget target, T entity, GenericType<List<R>> returnType) throws SessionExpiredException, ServerErrorException, ClientErrorException{
		return postEntityReturnList(target, entity, MediaType.APPLICATION_XML_TYPE, returnType, DEFAULT_RESP_TYPE);
	}
	
	protected <T, R> List<R> postEntityReturnList(WebTarget target, T entity, final MediaType postMediaType, GenericType<List<R>> returnType, MediaType returnMediaType) throws SessionExpiredException, ServerErrorException, ClientErrorException{
		Entity<T> ent = buildEntity(entity, postMediaType);
		Response resp = target.request(returnMediaType).post(ent);
		checkStatus(resp, target);
		List<R> genericList = extractList(resp, returnType);
		resp.close();
		return genericList;
	}	
	
	private <T> Entity<T> buildEntity(T entity, MediaType postMediaType){
		if(entity == null) {
			return null;
		}
		if(postMediaType == null) {
			postMediaType = MediaType.APPLICATION_JSON_TYPE;
		}
		Entity<T> ent = null;
		if(MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(postMediaType)) {
			/* FIXME
			 * there is a bug that prevents form params to be properly decoded from UTF-8 on the server 
			 * if the charset is not set explicitly!
			 * No idea where to fix this properly...
			 * Issue is solved by stating the charset here
			 */
			ent = Entity.entity(entity, postMediaType.toString() + ";charset=utf-8");
		} else {
			ent = Entity.entity(entity, postMediaType);
		}
		return ent;
	}
	
	private <T> T extractObject(Response resp, Class<T> returnType) {
		T object = null;
		try{
			object = resp.readEntity(returnType);
		} catch(ProcessingException | IllegalStateException e) {
			logger.error("Server response did not contain an object of type " + returnType.getSimpleName());
			throw e;
		} finally {
			resp.close();
		}
		return object;
	}

	<T> List<T> extractList(Response resp, GenericType<List<T>> returnType) throws ProcessingException {
		List<T> list = null;
		try{
			list = resp.readEntity(returnType);
		} catch(ProcessingException | IllegalStateException e) {
			logger.error("Server response did not contain a list of type " + returnType.getType());
			throw e;
		} finally {
			resp.close();
		}
		return list;
	}
	
	private static String readStringEntity(Response resp) {
		try {
			return resp.readEntity(String.class);
		} catch (ProcessingException e) {
			return "";
		}
	}
	
	/**
	 * TODO Unauthorized 403 => Session expired OR User is not authorized for this action
	 */
	protected void checkStatus(Response resp, WebTarget target) throws SessionExpiredException, ClientErrorException, ServerErrorException {
		final int status = resp.getStatus();
				
		final String loc = target.getUri().toString();
		if(status < 300) {
			//logger.debug(loc + " - " + status + " OK");
			return;
		} else if(status == 400) {
			throw new ClientErrorException(loc + " - Bad Request (400). "+readStringEntity(resp), resp);
		} else if(status == 401) {
			if (login != null)
				throw new SessionExpiredException(loc + " - Login expired (401).", login);
			else
				throw new SessionExpiredException(loc + " - Not logged in (401).");
		} else if(status == 403) {
			throw new ClientErrorException(loc + " - Forbidden request. (403) "+readStringEntity(resp), resp);
		} else if(status == 404) {
			throw new ClientErrorException(loc + " - Bad parameters. No entity found. (404) "+readStringEntity(resp), resp);
		} else if(status == 405) {
			throw new ClientErrorException(loc + " - Method not allowed! (405) "+readStringEntity(resp), resp);
		} 
//		else if(status == ClientVersionNotSupportedException.STATUS_CODE) {
//			throw new RuntimeException(loc + " - Method not allowed! (405) "+readStringEntity(resp)/*, resp*/);
//		}
		else if (status < 500) {
			throw new ClientErrorException("Client error: "+readStringEntity(resp), resp);
		}
		else { // 500 etc.
			String ent = readStringEntity(resp);
			
			throw new ServerErrorException(loc + " - Some server error occured! " + status + " - " + resp.getStatusInfo() + (StringUtils.isEmpty(ent)?"":" - "+ent), status);
		}
	}
	
	public class ClientStatus extends Observable implements Observer {
		private static final String STATUS_IDLE = "IDLE";
		private static final String STATUS_BUSY = "BUSY";
		public String status = "IDLE";

		@Override
		public void update(Observable o, Object arg) {
			setChanged();
			if (arg instanceof String) {
	            status = (String) arg;
	        }
			notifyObservers(status);
		}
		
		public void setIdle(){
			setChanged();
			status = STATUS_IDLE;
			notifyObservers(status);
		}
		
		public void setBusy(){
			setChanged();
			status = STATUS_BUSY;
			notifyObservers(status);
		}
	}
}
