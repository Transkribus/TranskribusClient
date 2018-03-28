package eu.transkribus.client.connection;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.mail.internet.ParseException;
import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.uri.UriComponent;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.transkribus.client.io.ASingleDocUpload;
import eu.transkribus.client.io.TrpDocUploadHttp;
import eu.transkribus.client.io.TrpDocUploadMultipart;
import eu.transkribus.client.io.TrpDocUploadZipHttp;
import eu.transkribus.client.util.BufferedFileBodyWriter;
import eu.transkribus.client.util.JerseyUtils;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.DocumentUploadDescriptor;
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.EdOption;
import eu.transkribus.core.model.beans.HtrModel;
import eu.transkribus.core.model.beans.KwsDocHit;
import eu.transkribus.core.model.beans.PageLock;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpCrowdProject;
import eu.transkribus.core.model.beans.TrpCrowdProjectMessage;
import eu.transkribus.core.model.beans.TrpCrowdProjectMilestone;
import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpEvent;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTotalTranscriptStatistics;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.TrpUpload;
import eu.transkribus.core.model.beans.TrpUpload.UploadType;
import eu.transkribus.core.model.beans.TrpWordgraph;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.enums.ScriptType;
import eu.transkribus.core.model.beans.enums.SearchType;
import eu.transkribus.core.model.beans.job.KwsParameters;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.mets.Mets;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.rest.JobErrorList;
import eu.transkribus.core.model.beans.rest.JobParameters;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.model.beans.searchresult.KeywordSearchResult;
import eu.transkribus.core.model.builder.CommonExportPars;
import eu.transkribus.core.model.builder.alto.AltoExportPars;
import eu.transkribus.core.model.builder.docx.DocxExportPars;
import eu.transkribus.core.model.builder.pdf.PdfExportPars;
import eu.transkribus.core.model.builder.tei.TeiExportPars;
import eu.transkribus.core.program_updater.HttpProgramPackageFile;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.rest.RESTConst;
import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.core.util.ProgressInputStream.ProgressInputStreamListener;

/**
 * Singleton implementation of ATrpServerConn.
 * @author philip
 *
 */
public class TrpServerConn extends ATrpServerConn {
	private static final Logger logger = LoggerFactory.getLogger(TrpServerConn.class);
		
	// Debugging URL. Access allowed from UIBK network only
	// final static String DEFAULT_URI = "http://dbis-faxe.uibk.ac.at:8080/TrpServer";
	
	// HTTPS URL. If using self-signed certificates, add it to the java keystore using $JAVA_HOME/bin/keytool.
//	public static final String DEFAULT_URI = "https://dbis-faxe.uibk.ac.at/TrpServer";
	
//	public static final String PROD_SERVER_URI = "https://transkribus.eu/TrpServer";
//	public static final String TEST_SERVER_URI = "https://transkribus.eu/TrpServerTesting";
//	public static final String OLD_TEST_SERVER_URI = "https://dbis-faxe.uibk.ac.at/TrpServerTesting";
//		
//	public static final String[] SERVER_URIS = new String[] {
//		PROD_SERVER_URI, 
//		TEST_SERVER_URI,
//		OLD_TEST_SERVER_URI
//	};	
//	public static final int DEFAULT_URI_INDEX = 0;
	
//	private static final GenericType<List<TrpTranscriptMetadata>> TRANS_MD_LIST_TYPE = new GenericType<List<TrpTranscriptMetadata>>() {};
//	private static final GenericType<List<TrpCollection>> COL_LIST_TYPE = new GenericType<List<TrpCollection>>() {};
//	private static final GenericType<List<TrpDocMetadata>> DOC_MD_LIST_TYPE = new GenericType<List<TrpDocMetadata>>() {};
//	private static final GenericType<List<TrpJobStatus>> JOB_LIST_TYPE = new GenericType<List<TrpJobStatus>>() {};
//	private static final GenericType<List<TrpWordgraph>> WORDGRAPH_LIST_TYPE = new GenericType<List<TrpWordgraph>>() {};
//	private static final GenericType<List<PageLock>> PAGELOCK_LIST_TYPE = new GenericType<List<PageLock>>() {};
//	private static final GenericType<List<EdFeature>> ED_FEATURE_LIST_TYPE = new GenericType<List<EdFeature>>() {};
//	private static final GenericType<List<TrpDocDir>> DOC_DIR_LIST_TYPE = new GenericType<List<TrpDocDir>>() {};
//	private static final GenericType<List<TrpEvent>> EVENT_LIST_TYPE = new GenericType<List<TrpEvent>>() {};
//	private static final GenericType<List<TrpDbTag>> DB_TAG_LIST_TYPE = new GenericType<List<TrpDbTag>>() {};
//	private static final GenericType<List<TestBean>> TEST_BEAN_LIST_TYPE = new GenericType<List<TestBean>>() {};

	// Singleton instance of this
//	private static TrpServerConn conn = null;
//	public ClientStatus status = new ClientStatus();
	
	//static Gson gson = new Gson();
	
	public TrpServerConn(String uriStr) throws LoginException {
		super(uriStr);
	}
	
	public TrpServerConn(String uriStr, final String username, final String password) throws LoginException {
		super(uriStr);
		login(username, password);
	}
	
	public TrpServerConn(TrpServer server) throws LoginException {
		this(server.getUriStr());
	}
	
	public TrpServerConn(TrpServer server, final String username, final String password) throws LoginException {
		this(server.getUriStr(), username, password);
	}
	
//	private TrpServerConn(String uriStr, final String user, final String pw) throws LoginException {
//		super(uriStr, user, pw);
//	}
	
//	public static TrpServerConn getInstance(final String uriStr) throws LoginException {
//		if (conn == null) {
//			logger.debug("Instantiating new TRP Server Connection...");
//			conn = new TrpServerConn(uriStr);
//		} else if (!conn.isSameServer(uriStr)) {
//			logger.debug("different server: "+uriStr+", current: "+conn.getServerUri()+" -> creating new connection instance!");
//			conn.logout();
//			conn = new TrpServerConn(uriStr);
//		}
//		return conn;
//	}
//	
//	public static TrpServerConn getInstance(final String uriStr, final String username, final String password) throws LoginException {
//		getInstance(uriStr);
//		conn.login(username, password);
//		
//		return conn;
//	}
	
//	/**
//	 * @param uriStr e.g. TrpServerConn.DEFAULT_URI
//	 * @param user
//	 * @param pw
//	 * @throws LoginException
//	 */
//	public static TrpServerConn getInstance(final String uriStr, final String user, final String pw)
//			throws LoginException {
//		if (conn == null) {
//			logger.debug("Instantiating new TRP Server Connection...");
//			conn = new TrpServerConn(uriStr, user, pw);
//		}
//		return conn;
//	}
	
//	public static TrpServerConn getInstance() throws LoginException {
//		if (conn == null) {
//			throw new LoginException("No connection found!");
//		}
//		return conn;
//	}
	
//	public void login(final String user, final String pw) {
//		conn.login(user, pw);
//	}
	
//	public void logout() throws ServerErrorException, IllegalArgumentException {
//		// on logout set singleton instance to null
////		conn = null;
//		super.logout();
//	}
	
	public void invalidate() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.AUTH_PATH).path(RESTConst.INVALIDATE_PATH);
		postNull(docTarget);
	}
	
	public void refreshSession() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.AUTH_PATH).path(RESTConst.REFRESH_PATH);
		super.postNull(docTarget);
	}
	
	/**
	 * Retrieves a TRP Document from the rest service. The bean includes doc
	 * metadata, the list of pages with image keys and (optionally) for each page a list of
	 * transcripts with xml keys
	 * 
	 * @param affiliation
	 * @param colId
	 * @param docId
	 * @param withTranscripts
	 * @return
	 * @throws SessionExpiredException 
	 */
	public TrpDoc getTrpDoc(final int colId, final int docId, int nrOfTranscriptsPerPage) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path("" + docId)
				.path(RESTConst.FULLDOC_PATH)
				.queryParam(RESTConst.NR_OF_TRANSCRIPTS_PARAM, ""+nrOfTranscriptsPerPage);
		return getObject(docTarget, TrpDoc.class);
	}
	
//	public TrpDoc getTrpDoc(final int colId, final int docId) throws SessionExpiredException, IllegalArgumentException {
//		return getTrpDoc(colId, docId, true);
//	}
	
	public List<TrpCollection> getAllCollections(int index, int nValues, String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(RESTConst.LIST_PATH)
				.queryParam(RESTConst.PAGING_INDEX_PARAM, index)
				.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues)
				.queryParam(RESTConst.SORT_COLUMN_PARAM, sortFieldName)
				.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection);
		return getList(target, COL_LIST_TYPE);
	}
	
	public int countAllCollections() throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(RESTConst.COUNT_PATH);
		return Integer.parseInt(getObject(target, String.class));
	}
	
	public void deleteCollection(int colId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId);
		
		delete(target);
	}
	
	public void deleteEmptyCollection(int colId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.DELETE_EMPTY_COLLECTION);
		
		postNull(target);
	}
	
	public int createCollection(String name) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(RESTConst.CREATE_COLLECTION_PATH);
		target = target.queryParam(RESTConst.COLLECTION_NAME_PARAM, name);
		
//		postNull(target);
		Integer cid = postNullReturnObject(target, Integer.class);
		return cid==null ? 0 : cid;
	}
	
	@Deprecated
	public void modifyCollection(int colId, String name) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.MODIFY_COLLECTION_PATH);
		target = target.queryParam(RESTConst.COLLECTION_NAME_PARAM, name);
		
		postNull(target);
	}
	
	public void updateCollectionMd(TrpCollection colMd) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(colMd == null) {
			throw new IllegalArgumentException("Collection object is null!");
		}
		final WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colMd.getColId())
				.path(RESTConst.MD_PATH);
		postEntity(docTarget, colMd, MediaType.APPLICATION_JSON_TYPE);
	}
	
	public int countDocs(int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.COUNT_PATH);
		
		return Integer.parseInt(getObject(target, String.class));
	}
	
	public int countUsersLoggedIn() throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.USER_PATH).path(RESTConst.COUNT_PATH);
		
		return Integer.parseInt(getObject(target, String.class));
	}
	
	public int countMyDocs() throws NumberFormatException, SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.USER_PATH).path(RESTConst.COUNT_MY_DOCS_PATH);
		return Integer.parseInt(getObject(target, String.class));
	}
		
	private WebTarget getAllDocsTarget(final int colId, int index, int nValues, String sortFieldName, String sortDirection) /*throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException*/ {
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.LIST_PATH)
				.queryParam(RESTConst.PAGING_INDEX_PARAM, index)
				.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues)
				.queryParam(RESTConst.SORT_COLUMN_PARAM, sortFieldName)
				.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection);
		return docTarget;
	}
	
	public List<TrpDocMetadata> getAllDocs(final int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		return getAllDocs(colId, 0, 0, null, null);
	}	
	
	public List<TrpDocMetadata> getAllDocs(final int colId, int index, int nValues, String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = getAllDocsTarget(colId, index, nValues, sortFieldName, sortDirection);
		return getList(docTarget, DOC_MD_LIST_TYPE);
	}
	
	public Future<List<TrpDocMetadata>> getAllDocsAsync(final int colId, int index, int nValues, String sortFieldName, String sortDirection, InvocationCallback<List<TrpDocMetadata>> callback) /*throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException*/ {
		WebTarget docTarget = getAllDocsTarget(colId, index, nValues, sortFieldName, sortDirection);
//		return docTarget.request(DOC_MD_LIST_TYPE).async().get(callback);
		return docTarget.request(DEFAULT_RESP_TYPE).async().get(callback);
	}
	
	public List<TrpDocMetadata> getDocCount(final int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.LIST_PATH);
		return getList(docTarget, DOC_MD_LIST_TYPE);
	}	
	
	private WebTarget getAllDocsByUserTarget(int index, int nValues, String sortFieldName, String sortDirection) {
		return baseTarget.path(RESTConst.USER_PATH).path(RESTConst.LIST_MY_DOCS_PATH)
						.queryParam(RESTConst.PAGING_INDEX_PARAM, index)
						.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues)
						.queryParam(RESTConst.SORT_COLUMN_PARAM, sortFieldName)
						.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection);
	}
	
	public List<TrpDocMetadata> getAllDocsByUser(int index, int nValues, String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = getAllDocsByUserTarget(index, nValues, sortFieldName, sortDirection);
		return getList(docTarget, DOC_MD_LIST_TYPE);
	}
	
	public Future<List<TrpDocMetadata>> getAllDocsByUserAsync(int index, int nValues, String sortFieldName, String sortDirection, InvocationCallback<List<TrpDocMetadata>> callback) {
		WebTarget docTarget = getAllDocsByUserTarget(index, nValues, sortFieldName, sortDirection);
		return docTarget.request(DEFAULT_RESP_TYPE).async().get(callback);
	}
	
//	public List<TrpTranscriptMetadata> getTranscriptMdList(final int colId, final int docId, final int pageNr) throws SessionExpiredException, ServerErrorException, IllegalArgumentException {
//		return getTranscriptMdList(colId, docId, pageNr, null, null);
//	}
	
	public List<TrpTranscriptMetadata> getTranscriptMdList(final int colId, final int docId, final int pageNr, final Integer index, final Integer nValues,
			String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		if (colId <= 0 || docId <= 0 || pageNr <= 0)
			return new ArrayList<>();		
		
		WebTarget docTarget = baseTarget
			.path(RESTConst.COLLECTION_PATH)
			.path(""+colId)
			.path("" + docId)
			.path("" + pageNr)
			.path(RESTConst.LIST_PATH)
			.queryParam(RESTConst.SORT_COLUMN_PARAM, sortFieldName)
			.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection);	
		
		if(index != null && nValues != null){
			docTarget = docTarget
					.queryParam(RESTConst.PAGING_INDEX_PARAM, index)
					.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues);
		}
		
		return getList(docTarget, TRANS_MD_LIST_TYPE);
	}
	
	public int countTranscriptMdList(final int colId, final int docId, final int pageNr) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		if (colId <= 0 || docId <= 0 || pageNr <= 0)
			return 0;
		
		WebTarget t = baseTarget
			.path(RESTConst.COLLECTION_PATH)
			.path(""+colId)
			.path("" + docId)
			.path("" + pageNr)
			.path(RESTConst.COUNT_PATH);
				
		return Integer.parseInt(getObject(t, String.class));
	}
	
	public List<TrpWordgraph> getWordgraphs(final int colId, final int docId, final int pageNr) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = baseTarget
			.path(RESTConst.COLLECTION_PATH)
			.path(""+colId)
			.path("" + docId)
			.path("" + pageNr)
			.path(RESTConst.WORDGRAPHS_PATH);
		
		return getList(docTarget, WORDGRAPH_LIST_TYPE);
	}
	
	
	/**
	 * FIXME docId is not really needed as param. as it is included in docMd
	 * 
	 * @param colId
	 * @param docId
	 * @param docMd
	 * @throws SessionExpiredException
	 * @throws IllegalArgumentException
	 * @throws ClientErrorException
	 */
	public void updateDocMd(final int colId, final int docId, TrpDocMetadata docMd) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path("" + docId)
				.path(RESTConst.MD_PATH);
		postEntity(docTarget, docMd, MediaType.APPLICATION_JSON_TYPE);
	}
	
	public void deleteDoc(int colId, int docId) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path("" + docId);
		delete(docTarget);
	}
	
	public void updateTagDefsCollection(final int colId, final String tagDefs) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId)
				.path(RESTConst.TAG_DEF_PARAM);
		postEntity(docTarget, tagDefs, MediaType.APPLICATION_JSON_TYPE);
	}
	
	public String getTagDefsCollection(final int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId)
				.path(RESTConst.LIST_TAG_DEFS_PARAM);
		
		String json = getObject(target, String.class);
		return json;
	}
	
	public void updateTagDefsUser(final String tagDefs) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.USER_PATH).path(RESTConst.TAG_DEF_PARAM);
		postEntity(docTarget, tagDefs, MediaType.APPLICATION_JSON_TYPE);
	}
	
	public String getTagDefsUser() throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.USER_PATH).path(RESTConst.LIST_TAG_DEFS_PARAM);
		
		String json = getObject(target, String.class);
		return json;
	}
	
//	public PcGtsType getTranscript(URL url) throws JAXBException, URISyntaxException, SessionExpiredException, ServerErrorException, IllegalArgumentException{
//		PcGtsType pc;
//		final String prot = url.getProtocol();
//		logger.debug("PROTOCOL: " + prot);
//		if(prot.equals("file") || prot.equals("http")){
//			pc = JaxbUtils.unmarshal(url, PcGtsType.class);
//		} else if(prot.equals("https")){
//			//TODO add Session ID
//			final WebTarget docTarget = client.target(url.toURI());
//			pc = getObject(docTarget, PcGtsType.class);
//		} else {
//			throw new IllegalArgumentException("Unknown URL protocol: " + prot + " in URL: " + url.toString());
//		}
//		return pc;
//	}
	
	
//	public PcGtsType getTranscript(final int docId, final int pageNr) throws SessionExpiredException, ServerErrorException, IllegalArgumentException {
//		return getTranscript(docId, pageNr, null);
//	}
//	
//	public PcGtsType getTranscript(final int docId, final int pageNr, final Long timestamp) throws SessionExpiredException, ServerErrorException, IllegalArgumentException {
//		PcGtsType pc;
//		WebTarget docTarget = baseTarget.path(RESTConst.DOC_PATH)
//				.path("" + docId)
//				.path("" + pageNr)
//				.path(RESTConst.TRANSCRIPT_PATH);
////		logger.info(docTarget.getUri().toString());
//		if(timestamp != null && timestamp != 0){
//				docTarget = docTarget.queryParam(RESTConst.TIMESTAMP, timestamp);
//		}
//		pc = getObject(docTarget, PcGtsType.class, MediaType.APPLICATION_XML_TYPE);
//		return pc;
//	}
	
	public void lockPage(final int colId, final int docId, final int pageNr) throws SessionExpiredException, ServerErrorException, ClientErrorException{
		WebTarget docTarget = baseTarget
				.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path("" + docId)
				.path("" + pageNr)
				.path(RESTConst.LOCK_PATH);
		postNull(docTarget);
	}
	
	public void unlockPage() throws SessionExpiredException, ServerErrorException, ClientErrorException{
		WebTarget docTarget = baseTarget
				.path(RESTConst.COLLECTION_PATH)
				.path(""+-1)
				.path("" + -1)
				.path("" + -1)
				.queryParam(RESTConst.TYPE_PARAM, true)
				.path(RESTConst.LOCK_PATH);
		postNull(docTarget);
	}
	
//	public void unlockPage(final int colId, final int docId, final int pageNr) throws SessionExpiredException, ServerErrorException{
//		WebTarget docTarget = baseTarget
//				.path(RESTConst.COLLECTION_PATH)
//				.path(""+colId)
//				.path("" + docId)
//				.path("" + pageNr)
//				.path(RESTConst.UNLOCK_PATH);
//		postNull(docTarget);
//	}
	
	public boolean isPageLocked(final int colId, final int docId, final int pageNr) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget
				.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path("" + docId)
				.path("" + pageNr)
				.path(RESTConst.TEST_LOCK_PATH);
		String isLockedStr = getObject(docTarget, String.class);
		return Boolean.parseBoolean(isLockedStr);
	}
	
	public List<PageLock> listPageLocks(int colId, int docId, int pageNr) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget
				.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path("" + docId)
				.path(""+ pageNr)
				.path(RESTConst.LIST_LOCKS_PATH);
		return getList(docTarget, PAGELOCK_LIST_TYPE);
	}
	
	public boolean canManageCollection(int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path(RESTConst.CAN_MANAGE_PATH);
		
		String canManage = getObject(target, String.class, MediaType.TEXT_PLAIN_TYPE);
		return Boolean.parseBoolean(canManage);
	}
	
	public String getLatestGuiVersion(boolean isRelease) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.CLIENT_VERSION_INFO_PATH);
		target = target.queryParam(RESTConst.IS_RELEASE_PARAM, isRelease);
		return getObject(target, String.class, MediaType.TEXT_PLAIN_TYPE);
	}
	
	public List<HttpProgramPackageFile> getAvailableClientFiles(boolean isRelease) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.CLIENT_AVAILABLE_FILES);
		target = target.queryParam(RESTConst.IS_RELEASE_PARAM, isRelease);
		
		return getList(target, new  GenericType<List<HttpProgramPackageFile>>(){});
	}
	
	public Future<List<HttpProgramPackageFile>> getAvailableClientFilesAsync(boolean isRelease, InvocationCallback<List<HttpProgramPackageFile>> callback) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		final WebTarget target = baseTarget
				.path(RESTConst.CLIENT_AVAILABLE_FILES).queryParam(RESTConst.IS_RELEASE_PARAM, isRelease);
		
		return target.request().async().get(callback);
	}
	
	public String test() {
		return "";
	}
	
	public <T> Future<T> invokeAsync(Supplier<T> supp) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		// TODO
//		Arrays.sort(a, c);
		String s="";
		
		invokeAsync(this::test);
		
		return null;
	}
		
	/**
	 * Download the latest version of the client
	 * @param f The file to store the latest client version to
	 * @param libs A map with the currently used jar-libs in the libs dir, including their md5 checksum. Can be null
	 * @param l A progress listener for the download. Can be null
	 * @deprecated A header field size greater 8kb leads to a server error which can happen if you specify a large libs map in this method!
	 */
	public String downloadClientFile(boolean isRelease, String filename, File f, Map<String, String> libs, ProgressInputStreamListener l) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, IOException, ParseException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.DOWNLOAD_CLIENT_FILE);
		target = target.queryParam(RESTConst.IS_RELEASE_PARAM, isRelease);
		target = target.queryParam(RESTConst.FILE_NAME_PARAM, filename);
		
		String encLibsStr = "", libsStr="";
		if (libs != null) {
			libsStr = new JSONObject(libs).toString();
//			encLibsStr = UriComponent.encode(new JSONObject(libs).toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
//			target = target.queryParam(RESTConst.LIBS_MAP_PARAM, encLibsStr); // can be too long --> include in header!
		}
		
		Builder b = target.request();
		if (!libsStr.isEmpty())
			b = b.header(RESTConst.LIBS_MAP_PARAM, libsStr);
		
		Response resp = b.get();
		
		checkStatus(resp, target);
		String fn = JerseyUtils.getFilenameFromContentDisposition(resp);
		
		JerseyUtils.downloadFile(resp, l, f);
		return fn;
	}	
	
	/**
	 * Download the latest version of the client
	 * @param f The file to store the latest client version to
	 * @param libs A map with the currently used jar-libs in the libs dir, including their md5 checksum. Can be null
	 * @param l A progress listener for the download. Can be null
	 */
	public String downloadClientFileNew(boolean isRelease, String filename, File f, Map<String, String> libs, ProgressInputStreamListener l) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, IOException, ParseException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.DOWNLOAD_CLIENT_FILE_NEW);
		target = target.queryParam(RESTConst.IS_RELEASE_PARAM, isRelease);
		target = target.queryParam(RESTConst.FILE_NAME_PARAM, filename);
		
		String encLibsStr = "", libsStr="";
		if (libs != null) {
			libsStr = new JSONObject(libs).toString();
//			encLibsStr = UriComponent.encode(new JSONObject(libs).toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
//			target = target.queryParam(RESTConst.LIBS_MAP_PARAM, encLibsStr); // can be too long --> include in header!
		}
		
		Builder b = target.request();
		Response resp = b.post(Entity.json(libsStr));
		
		checkStatus(resp, target);
		String fn = JerseyUtils.getFilenameFromContentDisposition(resp);
		
		JerseyUtils.downloadFile(resp, l, f);
		return fn;
	}	
	
	/**
	 * Download the latest version of the client
	 * @param f The file to store the latest client version to
	 * @param l A progress listener for the download. Can be null
	 * @throws SessionExpiredException
	 * @throws ServerErrorException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ParseException 
	 */
//	public String downloadLatestGuiVersion(boolean isRelease, File f, ProgressInputStreamListener l) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, IOException, ParseException, ClientErrorException {
//		WebTarget target = baseTarget
//				.path(RESTConst.DOWNLOAD_LATEST_CLIENT);
//		target = target.queryParam(RESTConst.IS_RELEASE_PARAM, isRelease);
//		Response resp = target.request().get();
//		checkStatus(resp, target);
//		
//		String fn = JerseyUtils.getFilenameFromContentDisposition(resp);
////		logger.debug("fn = "+fn);
//		JerseyUtils.downloadFile(resp, l, f);
//		
//		return fn;
//	}

	/**
	 * Just update the edit status for a transcript in the server
	 * 
	 * @param docId
	 * @param pageNr
	 * @param status
	 * @return Updated transcript list for this page
	 * @throws IllegalArgumentException 
	 * @throws ServerErrorException 
	 * @throws SessionExpiredException 
	 */
	public TrpTranscriptMetadata updateTranscriptStatus(final int colId, final int docId, int pageNr, EditStatus status, final int parentId, final String note) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		return updateTranscript(colId, docId, pageNr, status, null, false, parentId, note);
	}

	/**
	 * Update the Page XML for a page in the server
	 * @param docId
	 * @param pageNr
	 * @param status
	 *            new EditStatus. If null, the status from old version will be
	 *            used
	 * @param transcript
	 * @return Updated transcript list for this page
	 * @throws IllegalArgumentException 
	 * @throws ServerErrorException 
	 * @throws SessionExpiredException 
	 */
	public TrpTranscriptMetadata updateTranscript(final int colId, final int docId, int pageNr, EditStatus status,
			final PcGtsType transcript, final int parentId, final String note) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		return updateTranscript(colId, docId, pageNr, status, transcript, false, parentId, note);
	}
	
	public TrpTranscriptMetadata replaceCurrentTranscript(final int colId, final int docId, int pageNr,
			final PcGtsType transcript, final int parentId, final String note) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		return updateTranscript(colId, docId, pageNr, null, transcript, true, parentId, note);
	}
	
	/**
	 * Update the Page XML for a page in the server
	 * @param docId
	 * @param pageNr
	 * @param status
	 *            new EditStatus. If null, the status from old version will be
	 *            used
	 * @param transcript
	 * @return Updated transcript list for this page
	 * @throws IllegalArgumentException 
	 * @throws ServerErrorException 
	 * @throws SessionExpiredException 
	 */
	public TrpTranscriptMetadata updateTranscript(final int colId, final int docId, int pageNr, EditStatus status,
			final PcGtsType transcript, final boolean overwrite, final int parentId, final String note) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path("" + docId).path("" + pageNr)
				.path(RESTConst.TRANSCRIPT_PATH);
		if (status != null) {
			docTarget = docTarget.queryParam(RESTConst.STATUS_PARAM, status.toString());
		}
		docTarget = docTarget.queryParam(RESTConst.OVERWRITE_PARAM, overwrite)
				.queryParam(RESTConst.PARENT_ID_PARAM, parentId)
				.queryParam(RESTConst.NOTE_PARAM, note);
		return postXmlEntityReturnObject(docTarget, transcript, TrpTranscriptMetadata.class);
	}
	
	public void deleteTranscript(int colId, int docId, int pageNr, String key) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path("" + docId).path("" + pageNr)
				.path(RESTConst.DELETE_PATH);
		docTarget = docTarget.queryParam(RESTConst.KEY_PARAM, key);
		postNull(docTarget);
	}
	
	public void deleteUser(String username) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.USER_PATH).path(RESTConst.DELETE_PATH);
		
		t = t.queryParam(RESTConst.USER_PARAM, username);
		
		delete(t);
	}
	
	public TrpPage replacePageImage(final int colId, final int docId, final int pageNr, File imgFile, final IProgressMonitor monitor) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path("" + docId).path("" + pageNr)
				.path(RESTConst.REPLACE_PAGE_PATH);
		MultiPart mp = new MultiPart();
		mp.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
		
		if(monitor != null){
			BufferedFileBodyWriter bfbw = new BufferedFileBodyWriter();
			bfbw.addObserver(new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					if(arg instanceof Integer){
						monitor.worked((Integer)arg);
					}
				}});
			docTarget.register(bfbw);
			
		}
		
		FileDataBodyPart imgPart = new FileDataBodyPart("img", imgFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		mp.bodyPart(imgPart);
		
		return docTarget.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA), TrpPage.class);
	}
	
	
//	public Pair<TrpDocUploadSinglePages, Thread> postTrpDocSinglePages(final int colId, TrpDoc doc){
//		//create Empty doc with nr. of pages
//		
//		//start page upload thread
//	}
	
//	public Pair<TrpDocUploadZip, Thread> postTrpDoc(final int colId, TrpDoc doc) throws IOException{
//		return postTrpDoc(colId, doc, null);
//	}
	
	
//	/** This method fails when the zipped document is bigger than MAX_INTEGER-1 bytes as Jersey has that internal limit
//	 * @param doc"}/{"
//	 * @param o
//	 * @return
//	 * @throws IOException
//	 */
//	public Pair<TrpDocUploadZip, Thread> postTrpDoc(final int colId, TrpDoc doc, IProgressMonitor monitor) throws IOException{
//		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.UPLOAD_PATH);
//		if(doc == null) {
//			throw new IllegalArgumentException("TrpDoc is null!");
//		}
//				
//		//TODO connection extends observable!??
//		
//		TrpDocUploadZip upload = new TrpDocUploadZip(target, doc, monitor);
//		upload.call();
//		
////		upload.addObserver(status);
////		if (o!=null)
////			upload.addObserver(o);
//		
////		Thread t = new Thread(upload);
////		t.run();
//
//		return Pair.of(upload, t);
////		return upload;
//	}
	
//	public void testPostTrpDoc(File testFile) throws IOException{
//		WebTarget target = baseTarget.path(RESTConst.DOC_PATH).path(RESTConst.UPLOAD_PATH);
//		if(testFile == null) {
//			throw new IllegalArgumentException("file is null!");
//		}
//		
//		Entity<File> ent = Entity.entity(testFile, MediaType.APPLICATION_OCTET_STREAM);
//
//		final String sContentDisposition = "attachment; " + "filename=\"" + testFile.getName()
//				+ "\"; " + "size=" + testFile.length();
////		final String sContentLength = ""+testFile.length();
//		Integer.parseInt(""+testFile.length());
//		logger.info("File size = " + testFile.length() + ";");
//		
//		BufferedFileBodyWriter bfbw = new BufferedFileBodyWriter();
////		bfbw.addObserver(o);
//
//		target.register(bfbw);
//		Response response = target.request()
//				.header("Content-Disposition", sContentDisposition)
////				.header("Content-Length", sContentLength)
//				.post(ent);
//		logger.info("Request sent. " + response.getStatusInfo().toString());
//	}
	
	@Deprecated
	public Pair<TrpDocUploadMultipart, Thread> postTrpDocMultipart(final int colId, TrpDoc doc, Observer o) throws IOException{
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.UPLOAD_PATH_MULTIPART);
		if(doc == null) {
			throw new IllegalArgumentException("TrpDoc is null!");
		}
				
		//TODO connection extends observable!??
		
		TrpDocUploadMultipart upload = new TrpDocUploadMultipart(target, doc);
		BufferedFileBodyWriter bfbw = new BufferedFileBodyWriter();
		bfbw.addObserver(o);
		target.register(bfbw);
//		upload.addObserver(status);
		if (o!=null)
			upload.addObserver(o);
		
		Thread t = new Thread(upload);
		t.run();

		return Pair.of(upload, t);
//		return upload;
	}
	
	/**
	 * Use uploadTrpDoc() instead
	 * 
	 * @param colId
	 * @param doc
	 * @param monitor
	 * @throws Exception
	 */
	@Deprecated
	public void postTrpDoc(final int colId, TrpDoc doc, IProgressMonitor monitor) throws Exception {
		if (doc == null) {
			throw new IllegalArgumentException("TrpDoc is null!");
		}
		
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.UPLOAD_PATH);
		ASingleDocUpload upload = new TrpDocUploadZipHttp(target, doc, monitor);
		
		upload.call();
	}
	
	/**
	 * @param colId the collection where the created document will be linked to
	 * @param doc the local document entity
	 * @param type specifies the type of object that is used to submit the document's structure (JSON|METS)
	 * @param monitor IProgressMonitor
	 * @param obs only for debugging, can be null
	 * @return
	 * @throws Exception
	 */
	public TrpUpload uploadTrpDoc(final int colId, TrpDoc doc, UploadType type, boolean doMd5SumCheck, IProgressMonitor monitor, Observer obs) throws Exception {
		if (doc == null) {
			throw new IllegalArgumentException("TrpDoc is null!");
		}
		if(type == null) {
			type = UploadType.JSON;
		}
		ASingleDocUpload upload = new TrpDocUploadHttp(this, colId, doc, type, doMd5SumCheck, monitor);
		if(obs != null) {
			upload.addObserver(obs);
		}
		return (TrpUpload)upload.call();
	}
	
	/**
	 * @param colId the collection where the created document will be linked to
	 * @param doc the local document entity
	 * @param type specifies the type of object that is used to submit the document's structure (JSON|METS)
	 * @param monitor IProgressMonitor
	 * @return
	 * @throws Exception
	 */
	public TrpUpload uploadTrpDoc(final int colId, TrpDoc doc, UploadType type, boolean doMd5SumCheck, IProgressMonitor monitor) throws Exception {
		return uploadTrpDoc(colId, doc, type, doMd5SumCheck, monitor, null);
	}
	/**
	 * will use JSON for POSTing doc structure and set MD5 sums to be checked on the server
	 * 
	 * @param colId the collection where the created document will be linked to
	 * @param doc the local document entity
	 * @param monitor IProgressMonitor
	 * @return
	 * @throws Exception
	 */
	public TrpUpload uploadTrpDoc(final int colId, TrpDoc doc, IProgressMonitor monitor) throws Exception {
		return uploadTrpDoc(colId, doc, UploadType.JSON, true, monitor, null);
	}
	
	public List<TrpDocDir> listDocsOnFtp() throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.FILES_PATH).path(RESTConst.LIST_PATH);
		return getList(target, DOC_DIR_LIST_TYPE);
	}
	
	public Boolean checkDirOnFtp(final String dirName) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.FILES_PATH).path(RESTConst.CHECK_PATH);
		target = target.queryParam(RESTConst.FILE_NAME_PARAM, dirName);
		return Boolean.valueOf(super.getObject(target, String.class));
	}
	
	public void ingestDocFromFtp(final int colId, final String dirName, boolean checkForDuplicateTitle) throws Exception {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.INGEST_PATH);
		target = target.queryParam(RESTConst.FILE_NAME_PARAM, dirName).queryParam(RESTConst.CHECK_FOR_DUPLICATE_TITLE_PARAM, checkForDuplicateTitle);
		super.postNull(target);
	}
		
	public void ingestDocFromUrl(final int colId, final URL metsUrl) throws SessionExpiredException, ServerErrorException, ClientErrorException, UnsupportedEncodingException{
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.UPLOAD_PATH_METS_URL);
		String encodedUrlStr;
		try {
			//TODO will URL.toUri do the escaping? see java doc of URL
			encodedUrlStr = URLEncoder.encode(metsUrl.toString(), DEFAULT_URI_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Encoding not supported on this platform: " + DEFAULT_URI_ENCODING, e);
			throw e;
		}
		target = target.queryParam(RESTConst.FILE_NAME_PARAM, encodedUrlStr);
		super.postNull(target);

	}
	
	public void ingestDocFromLocalMetsUrl(final int colId, final URL metsUrl) throws SessionExpiredException, ServerErrorException, ClientErrorException, MalformedURLException, IOException{
		final WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.UPLOAD_PATH_METS);
		MultiPart mp = new MultiPart();
		mp.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
		
		final File mets = FileUtils.toFile(metsUrl);
		if(mets == null) {
			mp.close();
			throw new IllegalArgumentException("Not pointing to a file: " + metsUrl.toString()); 
		}
				
		//File.createTempFile("TRP", "mets");
		//mets.deleteOnExit();
		//FileUtils.copyURLToFile(new URL(metsUrlStr), mets);
		
		FileDataBodyPart imgPart = new FileDataBodyPart("mets", mets, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		mp.bodyPart(imgPart);
		
		target.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA));
		
		
		/*
		 * may needed here as well
		 */
//		String encodedUrlStr;
//		try {
//			encodedUrlStr = URLEncoder.encode(metsUrlStr, DEFAULT_URI_ENCODING);
//		} catch (UnsupportedEncodingException e) {
//			logger.error("Encoding not supported on this platform: " + DEFAULT_URI_ENCODING, e);
//			throw e;
//		}
	}
	
	public List<TrpJobStatus> getJobs(boolean filterByUser, String status, String type, Integer docId, int index, int nValues, String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOBS_PATH).path(RESTConst.LIST_PATH);
		//only get jobs of the logged in user?
		t = t.queryParam(RESTConst.FILTER_BY_USER_PARAM, filterByUser);
		t = t.queryParam(RESTConst.STATUS_PARAM, status);
		t = queryParam(t, RESTConst.TYPE_PARAM, type);
		t = t.queryParam(RESTConst.DOC_ID_PARAM, docId);
		t = t.queryParam(RESTConst.PAGING_INDEX_PARAM, index);
		t = t.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues);
		t = t.queryParam(RESTConst.SORT_COLUMN_PARAM, sortFieldName);
		t = t.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection);
		
//		//filter by docId
//		if(docId != null){
//			t = t.queryParam(RESTConst.DOC_ID_PARAM, docId);
//		}
		
		return getList(t, JOB_LIST_TYPE, MediaType.APPLICATION_XML_TYPE);		
	}
	
	public int countJobs(boolean filterByUser, String status, String type, Integer docId) throws NumberFormatException, SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.JOBS_PATH).path(RESTConst.COUNT_PATH);
		t = t.queryParam(RESTConst.FILTER_BY_USER_PARAM, filterByUser);
		t = t.queryParam(RESTConst.STATUS_PARAM, status);
		t = queryParam(t, RESTConst.TYPE_PARAM, type);
		t = t.queryParam(RESTConst.DOC_ID_PARAM, docId);
			
		return Integer.parseInt(getObject(t, String.class));
	}
	
//	public List<TrpJobStatus> getJobs(boolean filterByUser, String status, Integer docId, int index, int nValues, String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException{
//		return getJobs(filterByUser, status, null, index, nValues, sortFieldName, sortDirection);
//	}
	
	public TrpJobStatus getJob(final String jobId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		final WebTarget docTarget = baseTarget.path(RESTConst.JOBS_PATH).path("" + jobId);
		return getObject(docTarget, TrpJobStatus.class);
	}
	
	public void killJob(final String jobId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		final WebTarget docTarget = baseTarget.path(RESTConst.JOBS_PATH).path("" + jobId).path(RESTConst.KILL_JOB_PATH);
		postNull(docTarget);
	}
	
	public JobErrorList getJobErrors(final String jobId, final int index, final int nValues, final String sortColumnField, final String sortDirection) 
			throws SessionExpiredException, TrpServerErrorException, TrpClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.JOBS_PATH).path("" + jobId).path(RESTConst.ERROR_PATH);
		target = target.queryParam(RESTConst.PAGING_INDEX_PARAM, index);
		target = target.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues);
		target = target.queryParam(RESTConst.SORT_COLUMN_PARAM, sortColumnField);
		target = target.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection);		
		return super.getObject(target, JobErrorList.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	// NEW layout analysis client method
	public List<TrpJobStatus> analyzeLayout(int colId, List<DocumentSelectionDescriptor> dsds, 
			boolean doBlockSeg, boolean doLineSeg, boolean doWordSeg, boolean doPolygonToBaseline, boolean doBaselineToPolygon,
			String jobImpl, ParameterMap pars) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		
		if (jobImpl==null || !jobImpl.endsWith("LaJob")) {
			throw new IllegalArgumentException("Not a valid layout analysis job: "+jobImpl);
		}
		
		//use current prod. server version = true (does not accept ParameterMap yet)
		final boolean useOldEndpoint = false;
		//use new job workflow which is already deployed
		final boolean doCreateJobBatch = false;
		
		WebTarget target;
		if(useOldEndpoint) {
			target = baseTarget.path(RESTConst.LAYOUT_PATH).path(RESTConst.ANALYZE_PATH);
		} else {
			target = baseTarget.path(RESTConst.LAYOUT_PATH);
		}
		
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		
		if (doPolygonToBaseline) {
			target = target.queryParam(RESTConst.DO_BLOCK_SEG_PARAM, false);
			target = target.queryParam(RESTConst.DO_LINE_SEG_PARAM, false);
			target = target.queryParam(RESTConst.DO_WORD_SEG_PARAM, false);
			target = target.queryParam(RESTConst.DO_POLYGON_TO_BASELINE_PARAM, doPolygonToBaseline);
		} 
		else if (doBaselineToPolygon) {
			target = target.queryParam(RESTConst.DO_BLOCK_SEG_PARAM, false);
			target = target.queryParam(RESTConst.DO_LINE_SEG_PARAM, false);
			target = target.queryParam(RESTConst.DO_WORD_SEG_PARAM, false);
			target = target.queryParam(RESTConst.DO_BASELINE_TO_POLYGON_PARAM, doBaselineToPolygon);
		}
		else {
			target = target.queryParam(RESTConst.DO_BLOCK_SEG_PARAM, doBlockSeg);
			target = target.queryParam(RESTConst.DO_LINE_SEG_PARAM, doLineSeg);
			target = target.queryParam(RESTConst.DO_WORD_SEG_PARAM, doWordSeg);
		}
		
		target = target.queryParam(RESTConst.JOB_IMPL_PARAM, jobImpl);
		target = target.queryParam(RESTConst.DO_CREATE_JOB_BATCH_PARAM, doCreateJobBatch);
		
		if(useOldEndpoint) {
			//send this to endpoint that accepts the descriptor list
			GenericEntity<List<DocumentSelectionDescriptor>> entity = new GenericEntity<List<DocumentSelectionDescriptor>>(dsds) {};
			return postEntityReturnList(target, entity, MediaType.APPLICATION_XML_TYPE, 
					JOB_LIST_TYPE, MediaType.APPLICATION_XML_TYPE);
		} else {
			JobParameters jobParams = new JobParameters();
			jobParams.setDocs(dsds);
			jobParams.setParams(pars);
			return postEntityReturnList(target, jobParams, MediaType.APPLICATION_XML_TYPE, 
					JOB_LIST_TYPE, MediaType.APPLICATION_XML_TYPE);
		}
	}
	
	public List<String> getStringListTest() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.LAYOUT_PATH).path("getStringListTest");
		
		String json = getObject(target, String.class);
		return GsonUtil.toStrList2(json);
	}
	
	@Deprecated
	public String analyzeLayoutBatch(final int colId, final int docId, final String pages, final boolean doBlockSeg, final boolean doLineSeg) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.LAYOUT_PATH).path(RESTConst.ANALYZE_LAYOUT_BATCH_PATH);
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGES_PARAM, pages);
		target = target.queryParam(RESTConst.DO_BLOCK_SEG_PARAM, doBlockSeg);
		target = target.queryParam(RESTConst.DO_LINE_SEG_PARAM, doLineSeg);
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
			
	/**
	 * @param docId
	 * @param pageNr
	 * @param pc the current pageXml as shown in the MainWidget
	 * @param usePrintspaceOnly if true, then the region outside of the printspace is made transparent for block detection
	 * @return jobId
	 * @throws IOException
	 * @throws SessionExpiredException
	 * @throws IllegalArgumentException
	 */
	@Deprecated
	public String analyzeBlocks(final int colId, final int docId, final int pageNr, PcGtsType pc, final boolean usePrintspaceOnly) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.LAYOUT_PATH).path(RESTConst.ANALYZE_LAYOUT_PATH);
		if(pc == null) {
			throw new IllegalArgumentException("pcGtsType is null!");
		}
		target = target.queryParam(RESTConst.USE_PRINTSPACE_PARAM, usePrintspaceOnly);
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGE_NR_PARAM, pageNr);
		target = target.queryParam(RESTConst.PAGES_PARAM, pageNr);
		return postEntityReturnObject(target, pc, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	/**
	 * @param docId
	 * @param pageNr
	 * @param pc
	 * @param regIds A List of PAGE XML textRegion IDs. If not null, then the server will set the detected lines ONLY to those region. The rest of the PAGE XML will stay untainted, i.e. as shown in the MainWidget.
	 * @return
	 * @throws IOException
	 * @throws SessionExpiredException
	 * @throws IllegalArgumentException
	 */
	@Deprecated
	public String analyzeLines(final int colId, final int docId, final int pageNr, PcGtsType pc, List<String> regIds) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.LAYOUT_PATH).path(RESTConst.ANALYZE_LINES_PATH);
		return analyzeRegions(target, colId, docId, pageNr, pc, regIds);
	}
	@Deprecated
	public String analyzeWords(final int colId, final int docId, final int pageNr, PcGtsType pc, List<String> regIds) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.LAYOUT_PATH).path(RESTConst.ANALYZE_WORDS_PATH);
		return analyzeRegions(target, colId, docId, pageNr, pc, regIds);
	}
	@Deprecated
	public String addBaselines(final int colId, final int docId, final int pageNr, PcGtsType pc, List<String> regIds) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.LAYOUT_PATH).path(RESTConst.ANALYZE_BASELINES_PATH);
		return analyzeRegions(target, colId, docId, pageNr, pc, regIds);
	}
	@Deprecated
	private String analyzeRegions(WebTarget target, final int colId, int docId, int pageNr, PcGtsType pc,
			List<String> regIds) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		if(pc == null) {
			throw new IllegalArgumentException("pcGtsType is null!");
		}
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGE_NR_PARAM, pageNr);
		target = target.queryParam(RESTConst.PAGES_PARAM, pageNr);
		if(regIds != null && regIds.size() > 0){
			for(String r : regIds){
				target = target.queryParam(RESTConst.REG_ID_PARAM, r);
			}	
		}
		return postEntityReturnObject(target, pc, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public String runOcr(final int colId, final int docId, final String pageStr,
			final ScriptType typeFace, final String languages) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.OCR_PATH);
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGES_PARAM, pageStr);
		target = target.queryParam(RESTConst.TYPE_FACE_PARAM, typeFace.toString());
		target = target.queryParam(RESTConst.LANGUAGE_PARAM, languages);
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	@Deprecated
	public String runHtr(final int colId, final int docId, final int pageNr, final String modelName) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_PATH);
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGE_NR_PARAM, pageNr);
		target = target.queryParam(RESTConst.PAGES_PARAM, pageNr);
		target = target.queryParam(RESTConst.HTR_MODEL_NAME_PARAM, modelName);
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	@Deprecated
	public String runHtr(final int colId, final int docId, final String modelName) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_PATH);
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.HTR_MODEL_NAME_PARAM, modelName);
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public String runHtr(final int colId, final int docId, final String pageStr, final String modelName) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_PATH);
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGES_PARAM, pageStr);
		target = target.queryParam(RESTConst.HTR_MODEL_NAME_PARAM, modelName);
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public String runUpvlcHtrTraining(final String modelName, final Integer... docIds) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_UPVLC_TRAIN_PATH);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, (Object[])docIds);
		target = target.queryParam(RESTConst.HTR_MODEL_NAME_PARAM, modelName);
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public String runCitLabHtrTraining(final String modelName, final String numEpochs, final String learningRate,
			final String noise, final Integer trainSizePerEpoch, final Integer... docIds) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		return runCitLabHtrTraining(modelName, numEpochs, learningRate, noise, trainSizePerEpoch, null, docIds);
	}
	
	public String runCitLabHtrTraining(final String modelName, final String numEpochs, final String learningRate,
			final String noise, final Integer trainSizePerEpoch, final String baseModel, final Integer... docIds) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_URO_TRAIN_PATH);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, (Object[])docIds);
		target = target.queryParam(RESTConst.HTR_MODEL_NAME_PARAM, modelName);
		target = target.queryParam(JobConst.PROP_NUM_EPOCHS, numEpochs);
		target = target.queryParam(JobConst.PROP_LEARNING_RATE, learningRate);
		target = target.queryParam(JobConst.PROP_NOISE, noise);
		target = target.queryParam(JobConst.PROP_TRAIN_SIZE_PER_EPOCH, trainSizePerEpoch);
		target = target.queryParam(JobConst.PROP_BASE_MODEL, baseModel);
		
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public String runCitLabText2Image(CitLabSemiSupervisedHtrTrainConfig config) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		if(config == null) {
			throw new IllegalArgumentException("Config is null!");
		}
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_T2I_CITLAB_PATH);
				
		return postEntityReturnObject(target, config, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public String runCitLabHtrTraining(CitLabHtrTrainConfig config) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		if(config == null) {
			throw new IllegalArgumentException("Config is null!");
		}
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_CITLAB_TRAIN_PATH);
				
		return postEntityReturnObject(target, config, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}

	public void addHtrToCollection(final int htrId, final int colId, final int toColId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.RECOGNITION_PATH)
				.path(""+colId)
				.path(""+htrId)
				.path(RESTConst.ADD_PATH);
		target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, toColId);
		postNull(target);
	}
	
	public void removeHtrFromCollection(final int htrId, final int colId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.RECOGNITION_PATH)
				.path(""+colId)
				.path(""+htrId)
				.path(RESTConst.REMOVE_PATH);
		super.delete(target);
	}
	
	@Deprecated
	public List<String> getHtrModelListText() 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_LIST_MODEL_PATH);
		final String modelsStr = super.getObject(target, String.class, MediaType.TEXT_PLAIN_TYPE);
		return Arrays.asList(modelsStr.split("\n"));
	}
	
	/**
	 * LEGACY. Only used for HMM UPVLC HTR
	 * @return
	 * @throws SessionExpiredException
	 * @throws ServerErrorException
	 * @throws IllegalArgumentException
	 * @throws ClientErrorException
	 */
	@Deprecated
	public List<HtrModel> getHtrModelList() 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_LIST_MODEL_PATH);
		return super.getList(target, new GenericType<List<HtrModel>>(){});
	}
	
	public List<TrpHtr> getHtrs(final int colId, final String provider) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(""+colId).path(RESTConst.LIST_PATH);
		target = target.queryParam(RESTConst.PROVIDER_PARAM, provider);
		return super.getList(target, new GenericType<List<TrpHtr>>(){});
	}
	
	public void addOrModifyUserInCollection(int colId, int userId, TrpRole role) throws SessionExpiredException, ServerErrorException, ClientErrorException  {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
							.path(""+colId)
							.path(RESTConst.ADD_OR_MODIFY_USER_IN_COLLECTION)
							.queryParam(RESTConst.USER_ID_PARAM, userId)
							.queryParam(RESTConst.ROLE_PARAM, role.toString());
		
		postNull(target);
	}
	
	public void removeUserFromCollection(int colId, int userId) throws SessionExpiredException, ServerErrorException, ClientErrorException  {		
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
							.path(""+colId)
							.path(RESTConst.REMOVE_USER_FROM_COLLECTION)
							.queryParam(RESTConst.USER_ID_PARAM, userId);
		
		postNull(target);
	}
	
	public void addDocToCollection(int colId, int docId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path(RESTConst.ADD_DOC_TO_COLLECTION)
				.queryParam(RESTConst.DOC_ID_PARAM, docId);
		
		postNull(target);
	}
	
	public void removeDocFromCollection(int colId, int docId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path(RESTConst.REMOVE_DOC_FROM_COLLECTION)
				.queryParam(RESTConst.DOC_ID_PARAM, docId);
		
		postNull(target);
	}
	
	public String exportDocuments(int colId, List<DocumentSelectionDescriptor> dsds,			
			CommonExportPars commonPars,
			AltoExportPars altoPars,
			PdfExportPars pdfPars,
			TeiExportPars teiPars,
			DocxExportPars docxPars
			) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.EXPORT_PATH);
		
		if (commonPars!=null && !StringUtils.isEmpty(commonPars.getPages())) {
			target = target.queryParam(RESTConst.PAGES_PARAM, commonPars.getPages());
		}
				
		Map<String, String> parAsJsonMap = new HashMap<>();
		if (!CollectionUtils.isEmpty(dsds)) {
			parAsJsonMap.put(JobConst.PROP_DOC_DESCS, GsonUtil.toJson(dsds));
		}
		if (commonPars != null) {
			parAsJsonMap.put(CommonExportPars.PARAMETER_KEY, GsonUtil.toJson(commonPars));
		}
		if (altoPars != null) {
			parAsJsonMap.put(AltoExportPars.PARAMETER_KEY, GsonUtil.toJson(altoPars));
		}
		if (pdfPars != null) {
			parAsJsonMap.put(PdfExportPars.PARAMETER_KEY, GsonUtil.toJson(pdfPars));
		}
		if (teiPars != null) {
			parAsJsonMap.put(TeiExportPars.PARAMETER_KEY, GsonUtil.toJson(teiPars));
		}
		if (docxPars != null) {
			parAsJsonMap.put(DocxExportPars.PARAMETER_KEY, GsonUtil.toJson(docxPars));
		}
				
		return postEntityReturnObject(target, GsonUtil.toJson(parAsJsonMap), MediaType.APPLICATION_JSON_TYPE, 
		String.class, MediaType.APPLICATION_XML_TYPE);		
	}
	
	public String exportDocument(int colId, int docId,			
			CommonExportPars commonPars,
			AltoExportPars altoPars,
			PdfExportPars pdfPars,
			TeiExportPars teiPars,
			DocxExportPars docxPars
			) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(""+docId).path(RESTConst.EXPORT_PATH);
		
		if (commonPars!=null && !StringUtils.isEmpty(commonPars.getPages())) {
			target = target.queryParam(RESTConst.PAGES_PARAM, commonPars.getPages());
		}
				
		Map<String, String> parAsJsonMap = new HashMap<>();
		if (commonPars != null) {
			parAsJsonMap.put(CommonExportPars.PARAMETER_KEY, GsonUtil.toJson(commonPars));
		}
		if (altoPars != null) {
			parAsJsonMap.put(AltoExportPars.PARAMETER_KEY, GsonUtil.toJson(altoPars));
		}
		if (pdfPars != null) {
			parAsJsonMap.put(PdfExportPars.PARAMETER_KEY, GsonUtil.toJson(pdfPars));
		}
		if (teiPars != null) {
			parAsJsonMap.put(TeiExportPars.PARAMETER_KEY, GsonUtil.toJson(teiPars));
		}
		if (docxPars != null) {
			parAsJsonMap.put(DocxExportPars.PARAMETER_KEY, GsonUtil.toJson(docxPars));
		}
				
		return postEntityReturnObject(target, GsonUtil.toJson(parAsJsonMap), MediaType.APPLICATION_JSON_TYPE, 
		String.class, MediaType.APPLICATION_XML_TYPE);		
	}
	
	public String exportDocument_Old(int colId, int docId, 
			String pages, 
			boolean doWriteMets, boolean doWriteImages, boolean doExportPageXml, 
			boolean doExportAltoXml, boolean splitIntoWordsInAltoXml, boolean doWritePdf, boolean doWriteTei, boolean doWriteDocx,
			boolean doWriteTagsXlsx, boolean doWriteTablesXlsx, 
			boolean doPdfImagesOnly, boolean doPdfImagesPlusText, boolean doPdfWithTextPages, boolean doPdfWithTags, 
			boolean doTeiWithNoZones, boolean doTeiWithZonePerRegion, boolean doTeiWithZonePerLine, boolean doTeiWithZonePerWord, boolean doTeiWithLineTags, boolean doTeiWithLineBreaks, 
			boolean doDocxWithTags, boolean doDocxPreserveLineBreaks, boolean doDocxMarkUnclear, boolean doDocxKeepAbbrevs, boolean doDocxExpandAbbrevs, boolean doDocxSubstituteAbbrevs, 
			boolean doWordBased, boolean doBlackening,
			boolean doCreateTitle, String useVersionStatus) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path(""+docId)
				.path(RESTConst.EXPORT_PATH)
				.queryParam(RESTConst.PAGES_PARAM, pages)
				.queryParam(RESTConst.WRITE_METS_PARAM, doWriteMets)
				.queryParam(RESTConst.DO_WRITE_IMAGES_PARAM, doWriteImages)
				.queryParam(RESTConst.DO_EXPORT_PAGE_PARAM, doExportPageXml)
				.queryParam(RESTConst.DO_EXPORT_ALTO_PARAM, doExportAltoXml)
				.queryParam(RESTConst.DO_SPLIT_WORDS_IN_ALTO_PARAM, splitIntoWordsInAltoXml)
				.queryParam(RESTConst.WRITE_PDF_PARAM, doWritePdf)
				.queryParam(RESTConst.WRITE_TEI_PARAM, doWriteTei)
				.queryParam(RESTConst.WRITE_DOCX_PARAM, doWriteDocx)
				.queryParam(RESTConst.WRITE_TAGS_EXCEL_PARAM, doWriteTagsXlsx)
				.queryParam(RESTConst.WRITE_TABLES_EXCEL_PARAM, doWriteTablesXlsx)
				.queryParam(RESTConst.DO_PDF_IMAGES_ONLY_PARAM, doPdfImagesOnly)
				.queryParam(RESTConst.DO_PDF_IMAGES_PLUS_TEXT_PARAM, doPdfImagesPlusText)
				.queryParam(RESTConst.DO_PDF_EXTRA_TEXT_PARAM, doPdfWithTextPages)
				.queryParam(RESTConst.DO_PDF_HIGHLIGHT_TAGS_PARAM, doPdfWithTags)
				.queryParam(RESTConst.DO_TEI_NO_ZONES_PARAM, doTeiWithNoZones)
				.queryParam(RESTConst.DO_TEI_REGION_ZONE_PARAM, doTeiWithZonePerRegion)
				.queryParam(RESTConst.DO_TEI_LINE_ZONE_PARAM, doTeiWithZonePerLine)
				.queryParam(RESTConst.DO_TEI_WORD_ZONE_PARAM, doTeiWithZonePerWord)
				.queryParam(RESTConst.DO_TEI_LINE_TAGS_PARAM, doTeiWithLineTags)
				.queryParam(RESTConst.DO_TEI_LINE_BREAKS_PARAM, doTeiWithLineBreaks)
				.queryParam(RESTConst.DO_DOCX_EXPORT_TAGS, doDocxWithTags)
				.queryParam(RESTConst.DO_DOCX_PRESERVE_BREAKS_PARAM, doDocxPreserveLineBreaks)
				.queryParam(RESTConst.DO_DOCX_MARK_UNCLEAR_PARAM, doDocxMarkUnclear)
				.queryParam(RESTConst.DO_DOCX_KEEP_ABBREVS_PARAM, doDocxKeepAbbrevs)
				.queryParam(RESTConst.DO_DOCX_EXPAND_ABBREVS_PARAM, doDocxExpandAbbrevs)
				.queryParam(RESTConst.DO_DOCX_SUBSTITUTE_ABBREVS_PARAM, doDocxSubstituteAbbrevs)
				.queryParam(RESTConst.DO_WORD_BASED_EXPORT_PARAM, doWordBased)
				.queryParam(RESTConst.DO_BLACKENING_PARAM, doBlackening)
				.queryParam(RESTConst.DO_CREATE_TITLE_PARAM, doCreateTitle)
				.queryParam(RESTConst.USE_VERSION_STATUS_PARAM, useVersionStatus);
		
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}

	public List<TrpUser> findUsers(String username, String firstName, String lastName, boolean exactMatch, boolean caseSensitive) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget target = baseTarget.path(RESTConst.USER_PATH).path(RESTConst.FIND_USER_PATH);

		target = target.queryParam(RESTConst.USER_PARAM, username)
						.queryParam(RESTConst.FIRST_NAME_PARAM, firstName)
						.queryParam(RESTConst.LAST_NAME_PARAM, lastName)
						.queryParam(RESTConst.EXACT_MATCH_PARAM, exactMatch)
						.queryParam(RESTConst.CASE_SENSITIVE_PARAM, caseSensitive);
		
		return getList(target, new GenericType<List<TrpUser>>(){});
	}
	
//	@QueryParam(RESTConst.DESCRIPTION_PARAM) String description,
//	@QueryParam(RESTConst.AUTHOR_PARAM) String author,
//	@QueryParam(RESTConst.WRITER_PARAM) String writer,
	
	public List<TrpDocMetadata> findDocuments(int collId, Integer docId, String title, String description, String author, String writer, boolean exactMatch, boolean caseSensitive, int index, int nValues, String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(RESTConst.FIND_DOCUMENTS_PATH);
	
		target = target	.queryParam(RESTConst.COLLECTION_ID_PARAM, collId)	
						.queryParam(RESTConst.TITLE_PARAM, title)
						.queryParam(RESTConst.DESCRIPTION_PARAM, description)
						.queryParam(RESTConst.AUTHOR_PARAM, author)
						.queryParam(RESTConst.WRITER_PARAM, writer)
						.queryParam(RESTConst.DOC_ID_PARAM, docId)
						
						.queryParam(RESTConst.EXACT_MATCH_PARAM, exactMatch)
						.queryParam(RESTConst.CASE_SENSITIVE_PARAM, caseSensitive)
						.queryParam(RESTConst.PAGING_INDEX_PARAM, index)
						.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues)
						.queryParam(RESTConst.SORT_COLUMN_PARAM, sortFieldName)
						.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection)
						;
		
		return getList(target, new GenericType<List<TrpDocMetadata>>(){});
	}
	
	public int countFindDocuments(int collId, Integer docId, String title, String description, String author, String writer, boolean exactMatch, boolean caseSensitive) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(RESTConst.COUNT_FIND_DOCUMENTS_PATH);
	
		target = target	.queryParam(RESTConst.COLLECTION_ID_PARAM, collId)	
						.queryParam(RESTConst.TITLE_PARAM, title)
						.queryParam(RESTConst.DESCRIPTION_PARAM, description)
						.queryParam(RESTConst.AUTHOR_PARAM, author)
						.queryParam(RESTConst.WRITER_PARAM, writer)
						.queryParam(RESTConst.DOC_ID_PARAM, docId)
						
						.queryParam(RESTConst.EXACT_MATCH_PARAM, exactMatch)
						.queryParam(RESTConst.CASE_SENSITIVE_PARAM, caseSensitive)
						;
		
		return Integer.parseInt(getObject(target, String.class));
	}	

//	public List<TrpUser> getUserList() throws SessionExpiredException, ServerErrorException, IllegalArgumentException{
//		WebTarget target = baseTarget.path(RESTConst.AUTH_PATH).path(RESTConst.LIST_USERS_PATH);
//		return getList(target, new  GenericType<List<TrpUser>>(){});		
//	}
	
	/**
	 * Return users for a given collection. Set role to null if you want users with all roles.
	 */
	public List<TrpUser> getUsersForCollection(int colId, TrpRole role, int index, int nValues, String sortFieldName, String sortDirection) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.LIST_USERS_PATH).queryParam(RESTConst.ROLE_PARAM, role)
						.queryParam(RESTConst.PAGING_INDEX_PARAM, index)
						.queryParam(RESTConst.PAGING_NVALUES_PARAM, nValues)
						.queryParam(RESTConst.SORT_COLUMN_PARAM, sortFieldName)
						.queryParam(RESTConst.SORT_DIRECTION_PARAM, sortDirection);	
		return getList(t, new GenericType<List<TrpUser>>(){});
	}
	
	public int countUsersForCollection(final int colId, TrpRole role) throws NumberFormatException, SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		
		WebTarget t = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.LIST_USERS_PATH).path(RESTConst.COUNT_PATH)
				.queryParam(RESTConst.ROLE_PARAM, role);
		
		return Integer.parseInt(getObject(t, String.class));
	}
	
//	public void allow(String userName, int docId, TrpRole role) throws SessionExpiredException, ServerErrorException{
//		WebTarget target = baseTarget.path(RESTConst.DOC_PATH).path(""+docId).path(RESTConst.ALLOW_PATH);
//		if(userName == null || role == null){
//			throw new IllegalArgumentException("A parameter is null!");
//		}
//		target = target.queryParam(RESTConst.USER_PARAM, userName);
//		target = target.queryParam(RESTConst.ROLE_PARAM, role.toString()); 	
//		super.postNull(target);
//	}
//	
//	public void disallow(String userName, int docId) throws SessionExpiredException, ServerErrorException {
//		WebTarget target = baseTarget.path(RESTConst.DOC_PATH).path(""+docId).path(RESTConst.DISALLOW_PATH);
//		if(userName == null){
//			throw new IllegalArgumentException("userName is null!");
//		}
//		target = target.queryParam(RESTConst.USER_PARAM, userName); 	
//		super.postNull(target);
//	}
	
	// TODO / FIXME ? 
	public void sendBugReport(String email, String subject, String message, boolean isBug, boolean sendCopyToEmail, File tailOfLogFile) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		if (email == null || subject == null || message == null)
			throw new IllegalArgumentException("email, subject or message not defined!");
		
		WebTarget target = baseTarget.path(RESTConst.BUG_REPORT_PATH);
		FormDataMultiPart multiPart = new FormDataMultiPart();
			    
	    multiPart.field(RESTConst.EMAIL_PARAM, email);
	    multiPart.field(RESTConst.SUBJECT_PARAM, subject);
	    multiPart.field(RESTConst.MESSAGE_PARAM, message);
	    multiPart.field(RESTConst.IS_BUG_PARAM, ""+isBug);
	    multiPart.field(RESTConst.OPTS_PARAM, ""+sendCopyToEmail);
		if (tailOfLogFile != null) {
			FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(RESTConst.LOG_FILE_PARAM,
					tailOfLogFile,
		            MediaType.APPLICATION_OCTET_STREAM_TYPE);
			multiPart.bodyPart(fileDataBodyPart);
		}	    
	    
	    super.postEntity(target, multiPart, multiPart.getMediaType());
	}
	
	public PcGtsType analyzePageStructure(int colId, int docId, int pageNr, 
			boolean detectPageNumbers, boolean detectRunningTitles, boolean detectFootnotes) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, JAXBException, ClientErrorException {
		
		WebTarget t = baseTarget.path(RESTConst.STRUCTURE_PATH).path(RESTConst.ANALYZE_STRUCTURE_PATH);
		t = t.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		t = t.queryParam(RESTConst.ID_PARAM, docId);
		t = t.queryParam(RESTConst.PAGE_NR_PARAM, pageNr);
		t = t.queryParam(RESTConst.PAGES_PARAM, pageNr);
		
		t = t.queryParam(RESTConst.DETECT_PAGE_NUMBERS_PARAM, detectPageNumbers);
		t = t.queryParam(RESTConst.DETECT_RUNNING_TITLES_PARAM, detectRunningTitles);
		t = t.queryParam(RESTConst.DETECT_FOOTNOTES_PARAM, detectFootnotes);
		
		String xmlStr = getObject(t, String.class, MediaType.TEXT_PLAIN_TYPE);
		logger.debug("returned xmlStr: \n\n"+xmlStr);
		
		return PageXmlUtils.unmarshal(xmlStr);
	}

	//Editorial Declaration stuff ================================
	
	/**list all available editorial declaration features for this collection (includes also the presets with colId = null)
	 * @param colId
	 * @return
	 * @throws SessionExpiredException
	 * @throws ServerErrorException
	 * @throws IllegalArgumentException
	 */
	public List<EdFeature> getEditDeclFeatures(int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.LIST_EDIT_DECL_FEATURES);
		return super.getList(docTarget, ED_FEATURE_LIST_TYPE);
	}
	
	/** store a new editorial declaration feature. If user is admin colId may be null. then feature is a global preset
	 * @param colId
	 * @param feature
	 * @throws SessionExpiredException
	 * @throws ServerErrorException
	 * @throws IllegalArgumentException
	 */
	public void postEditDeclFeature(Integer colId, EdFeature feature) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.STORE_EDIT_DECL_FEATURE);
		super.postEntity(docTarget, feature, MediaType.APPLICATION_XML_TYPE);
	}
	
	public void deleteEditDeclFeature(Integer colId, EdFeature feature) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.DELETE_EDIT_DECL_FEATURE)
				.queryParam(RESTConst.ID_PARAM, feature.getFeatureId());
		super.postNull(docTarget);
	}
	
	public void postCrowdProject(Integer colId, TrpCrowdProject project) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.STORE_CROWD_PROJECT);
		super.postEntity(docTarget, project, MediaType.APPLICATION_XML_TYPE);
	}
		
	public int postCrowdProjectMilestone(Integer colId, TrpCrowdProjectMilestone currMst) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.STORE_CROWD_PROJECT_MILESTONE);
		//target = target.queryParam(RESTConst.CROWD_PROJECT_MILESTONE, currMst);
		
//		postNull(target);
		Integer cid = postNullReturnObject(target, currMst, MediaType.APPLICATION_XML_TYPE, Integer.class);
		return cid==null ? 0 : cid;
	}
	
	public int postCrowdProjectMessage(Integer colId, TrpCrowdProjectMessage currMsg) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.STORE_CROWD_PROJECT_MESSAGE);
		//target = target.queryParam(RESTConst.CROWD_PROJECT_MESSAGE, currMsg);
		
//		postNull(target);
		//Integer cid = postNullReturnObject(target, Integer.class);
		Integer cid = postNullReturnObject(target, currMsg, MediaType.APPLICATION_XML_TYPE, Integer.class);
		return cid==null ? 0 : cid;
	}
	
	public void deleteCrowdProjectMilestones(int colId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.DELETE_CROWD_PROJECT_MILESTONES)
				.queryParam(RESTConst.ID_PARAM, -1);
		delete(target);
		//super.postNull(docTarget);
	}
	
	public void deleteCrowdProjectMilestone(int colId, int id) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.DELETE_CROWD_PROJECT_MILESTONES)
				.queryParam(RESTConst.ID_PARAM, id);
		delete(target);
		
	}
	
	public void deleteCrowdProjectMessages(int colId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.DELETE_CROWD_PROJECT_MESSAGES)
				.queryParam(RESTConst.ID_PARAM, -1);
		delete(target);
		//super.postNull(docTarget);
	}
	
	public void deleteCrowdProjectMessage(int colId, int id) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.DELETE_CROWD_PROJECT_MESSAGES)
				.queryParam(RESTConst.ID_PARAM, id);
		delete(target);
		
	}

	public void postEditDeclOption(Integer colId, EdOption option) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.STORE_EDIT_DECL_OPTION);
		super.postEntity(docTarget, option, MediaType.APPLICATION_XML_TYPE);
	}

	public void deleteEditDeclOption(int colId, EdOption opt) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(RESTConst.DELETE_EDIT_DECL_OPTION)
				.queryParam(RESTConst.ID_PARAM, opt.getOptionId());
		super.postNull(docTarget);
	}
	
//	public void updateEditDeclFeature(EdFeature feature){
//		return;
//	}
//	public void updateEditDeclOptionText(EdOption option){
//		return;
//	}
	public List<EdFeature> getEditDeclByDoc(int colId, int docId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(""+docId).path(RESTConst.EDIT_DECL_PATH);
		return super.getList(docTarget, ED_FEATURE_LIST_TYPE);
	}
	public void postEditDecl(int colId, int docId, List<EdFeature> features) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(""+docId).path(RESTConst.EDIT_DECL_PATH);
		final EdFeature[] featArr = features.toArray(new EdFeature[features.size()]);
		super.postEntity(docTarget, featArr, MediaType.APPLICATION_XML_TYPE);
	}
	
	public String computeWer(String refKey, String hypKey) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		if(refKey == null || hypKey == null){
			throw new IllegalArgumentException("A key is null!");
		}
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.WER_PATH);
		target = target.queryParam(RESTConst.REF_KEY_PARAM, refKey);
		target = target.queryParam(RESTConst.KEY_PARAM, hypKey);
		return super.getObject(target, String.class, MediaType.TEXT_PLAIN_TYPE);
	}
	
	public List<TrpEvent> getNextEvents(int days) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget
				.path(RESTConst.EVENTS_PATH);
		target = target.queryParam(RESTConst.TIMESTAMP, days);
		return getList(target, EVENT_LIST_TYPE);
	}
	
	public String duplicateDocument(final int colId, final int docId, final String targetDocName, final Integer toColId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException{
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path(""+docId)
				.path(RESTConst.DUPLICATE_PATH)
				.queryParam(RESTConst.NAME_PARAM, targetDocName)
				.queryParam(RESTConst.COLLECTION_ID_PARAM, toColId);
		return postEntityReturnObject(docTarget, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}

	@Deprecated
	public List<KwsDocHit> doKwsSearch(int colId, Integer docId, String term, int confidence) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId);
		if(docId != null && docId != 0){
			docTarget = docTarget.path(""+docId);
		}
		docTarget = docTarget.path(RESTConst.KWS_SEARCH_PATH)
				.queryParam(RESTConst.TEXT_PARAM, term)
				.queryParam(RESTConst.CONFIDENCE_PARAM, confidence);
		return super.getList(docTarget, new GenericType<List<KwsDocHit>>() {});
	}
	
	@Deprecated
	public String runRnnHtr(final int colId, final int docId, final String pageStr, final String modelName, final String dictName) 
				throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
			WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_RNN_PATH);
			target = target.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
			target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
			target = target.queryParam(RESTConst.PAGES_PARAM, pageStr);
			target = target.queryParam(RESTConst.HTR_MODEL_NAME_PARAM, modelName);
			target = target.queryParam(RESTConst.HTR_DICT_NAME_PARAM, dictName);
			return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
					String.class, MediaType.APPLICATION_XML_TYPE);
	}
	

	public String runCitLabHtr(int colId, int docId, String pages, final int modelId, final String dictName) throws SessionExpiredException, TrpServerErrorException, TrpClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH)
				.path(""+colId)
				.path(""+modelId)
				.path(RESTConst.HTR_CITLAB_TEST_PATH);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGES_PARAM, pages);
		target = target.queryParam(RESTConst.HTR_DICT_NAME_PARAM, dictName);
		return postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.TEXT_PLAIN_TYPE);
	}
	
	public String runCitLabHtr(int colId, DocumentSelectionDescriptor descriptor, final int modelId, final String dictName) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(descriptor == null || descriptor.getPages().isEmpty()) {
			throw new IllegalArgumentException("descriptor must not be null or empty!");
		}
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH)
				.path(""+colId)
				.path(""+modelId)
				.path(RESTConst.HTR_CITLAB_TEST_PATH);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, descriptor.getDocId());
		target = target.queryParam(RESTConst.HTR_DICT_NAME_PARAM, dictName);
		return postEntityReturnObject(target, descriptor, MediaType.APPLICATION_JSON_TYPE, 
				String.class, MediaType.TEXT_PLAIN_TYPE);
	}
	
	@Deprecated
	public List<String> getHtrRnnListText() 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_LIST_NETS_PATH);
		final String modelsStr = super.getObject(target, String.class, MediaType.TEXT_PLAIN_TYPE);
		return Arrays.asList(modelsStr.split("\n"));
	}
	
	@Deprecated
	public List<String> getHtrDictListText() 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.RECOGNITION_PATH).path(RESTConst.HTR_LIST_DICTS_PATH);
		final String modelsStr = super.getObject(target, String.class, MediaType.TEXT_PLAIN_TYPE);
		return new ArrayList<String>(Arrays.asList(modelsStr.split("\n")));
	}
	
	public Future<FulltextSearchResult> searchFulltextAsync(String query,
			SearchType type,
			Integer start,
			Integer rows,
			final List<String> filters,
			InvocationCallback<FulltextSearchResult> callback
			) throws SessionExpiredException, ServerErrorException, ClientErrorException, UnsupportedEncodingException { 
		WebTarget target = baseTarget.path(RESTConst.SEARCH_PATH).path(RESTConst.FULLTEXT_PATH);
		
		target = target	.queryParam(RESTConst.QUERY_PARAM, query)	
						.queryParam(RESTConst.TYPE_PARAM, type.toString());
		if(start != null) {
			target = target.queryParam(RESTConst.START_PARAM, start);
		}
		if(rows != null) {
			target = target.queryParam(RESTConst.ROWS_PARAM, rows);
		}
		if(filters != null && !filters.isEmpty()) {
			for(String f : filters){
				target = target.queryParam(RESTConst.FILTER_PARAM, f);
			}	
		}
//		return super.getObject(target, FulltextSearchResult.class, MediaType.APPLICATION_JSON_TYPE);
		return target.request(MediaType.APPLICATION_JSON_TYPE).async().get(callback);
	}
	
	public Future<KeywordSearchResult> searchKWAsync(
			String keyword,
			Integer start,
			Integer rows,
			Float probL,
			Float probH,
			final List<String> filters,
			String sorting,
			Integer fuzzy,
			InvocationCallback<KeywordSearchResult> callback
			){
		
		WebTarget target = baseTarget.path(RESTConst.SEARCH_PATH).path(RESTConst.KEYWORD_PATH);
		
		target = target	.queryParam(RESTConst.QUERY_PARAM, keyword);
		if(start != null) {
			target = target.queryParam(RESTConst.START_PARAM, start);
		}
		if(rows != null) {
			target = target.queryParam(RESTConst.ROWS_PARAM, rows);
		}		
		if(probL != null) {
			target = target.queryParam(RESTConst.PROBL_PARAM, probL);
		}
		if(probH != null) {
			target = target.queryParam(RESTConst.PROBH_PARAM, probH);
		}		
		if(filters != null && !filters.isEmpty()) {
			for(String f : filters){
				target = target.queryParam(RESTConst.FILTER_PARAM, f);
			}	
		}
		if(fuzzy != null) {
			target = target.queryParam(RESTConst.FUZZY_PARAM, fuzzy);
		}
		if(sorting != null) {
			target = target.queryParam(RESTConst.SORTING_PARAM, sorting);
		}
		
		return target.request(MediaType.APPLICATION_JSON_TYPE).async().get(callback);
	}
	
	@Deprecated
	public FulltextSearchResult searchFulltext(String query,
			SearchType type,
			Integer start,
			Integer rows,
			final List<String> filters
			) throws SessionExpiredException, ServerErrorException, ClientErrorException { 
		WebTarget target = baseTarget.path(RESTConst.SEARCH_PATH).path(RESTConst.FULLTEXT_PATH);

		target = target	.queryParam(RESTConst.QUERY_PARAM, query)	
						.queryParam(RESTConst.TYPE_PARAM, type.toString());
		if(start != null) {
			target = target.queryParam(RESTConst.START_PARAM, start);
		}
		if(rows != null) {
			target = target.queryParam(RESTConst.ROWS_PARAM, rows);
		}
		if(filters != null && !filters.isEmpty()) {
			for(String f : filters){
				target = target.queryParam(RESTConst.FILTER_PARAM, f);
			}	
		}
		
		return super.getObject(target, FulltextSearchResult.class, MediaType.APPLICATION_JSON_TYPE);
	}
	
	public WebTarget getSearchTagsTarget(
			Set<Integer> collIds,
			Set<Integer> docIds,
			Set<Integer> pageIds,
			String tagName,
			String tagValue,
			String regionType,
			boolean exactMatch,
			boolean caseSensitive,
			Map<String, Object> attributes
			) {
		
		Gson gson = new Gson();
		String attributesJson;
		try {
			attributesJson = URLEncoder.encode(gson.toJson(attributes),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Unable to encode attributes map: "+e.getMessage());
			attributesJson = null;
		}
		logger.debug("attributesJson = "+attributesJson);
		
		WebTarget t = baseTarget.path(RESTConst.SEARCH_PATH).path(RESTConst.TAGS_PATH);
		t = JerseyUtils.queryParam(t, RESTConst.COLLECTION_ID_PARAM, collIds);
		t = JerseyUtils.queryParam(t, RESTConst.DOC_ID_PARAM, docIds);
		t = JerseyUtils.queryParam(t, RESTConst.PAGE_ID_PARAM, pageIds);
		t = JerseyUtils.queryParam(t, RESTConst.TAG_NAME_PARAM, tagName);
		t = JerseyUtils.queryParam(t, RESTConst.TAG_VALUE_PARAM, tagValue);
		t = JerseyUtils.queryParam(t, RESTConst.REGION_TYPE_PARAM, regionType);
		t = JerseyUtils.queryParam(t, RESTConst.EXACT_MATCH_PARAM, exactMatch);
		t = JerseyUtils.queryParam(t, RESTConst.CASE_SENSITIVE_PARAM, caseSensitive);
		t = JerseyUtils.queryParam(t, RESTConst.ATTRIBUTES_PARAM, attributesJson);
		
		return t;
	}
	
	public Future<List<TrpDbTag>> searchTagsAsync(
			Set<Integer> collIds,
			Set<Integer> docIds,
			Set<Integer> pageIds,
			String tagName,
			String tagValue,
			String regionType,
			boolean exactMatch,
			boolean caseSensitive,		
			Map<String, Object> attributes, InvocationCallback<List<TrpDbTag>> callback) /*throws SessionExpiredException, ServerErrorException, IllegalArgumentException, ClientErrorException*/ {
		WebTarget t = getSearchTagsTarget(collIds, docIds, pageIds, tagName, tagValue, regionType, exactMatch, caseSensitive, attributes);
//		return docTarget.request(DOC_MD_LIST_TYPE).async().get(callback);
		return t.request(DEFAULT_RESP_TYPE).async().get(callback);
	}

	public List<TrpDbTag> searchTags(
				Set<Integer> collIds,
				Set<Integer> docIds,
				Set<Integer> pageIds,
				String tagName,
				String tagValue,
				String regionType,
				boolean exactMatch,
				boolean caseSensitive,		
				Map<String, Object> attributes
			) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = getSearchTagsTarget(collIds, docIds, pageIds, tagName, tagValue, regionType, exactMatch, caseSensitive, attributes);
				
		return getList(t, DB_TAG_LIST_TYPE);
	}
	
	public void updatePageStatus(final int colId, final int docId, final int pageNr, final int transcriptId,
			final EditStatus status, final String note) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId)
				.path(""+docId)
				.path(""+pageNr)
				.path(""+transcriptId);
		target = target.queryParam(RESTConst.STATUS_PARAM, status.toString());
		target = target.queryParam(RESTConst.NOTE_PARAM, note);
		super.postNull(target);
	}
	
	public TrpDoc getHtrTrainDoc(final int colId, final int htrId, int nrOfTranscriptsPerPage) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.RECOGNITION_PATH).path(""+colId).path("" + htrId)
				.path(RESTConst.TRAIN_DOC_PATH)
				.queryParam(RESTConst.NR_OF_TRANSCRIPTS_PARAM, ""+nrOfTranscriptsPerPage);
		return getObject(docTarget, TrpDoc.class);
	}
	
	public TrpDoc getHtrTestDoc(final int colId, final int htrId, int nrOfTranscriptsPerPage) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.RECOGNITION_PATH).path(""+colId).path("" + htrId)
				.path(RESTConst.TEST_DOC_PATH)
				.queryParam(RESTConst.NR_OF_TRANSCRIPTS_PARAM, ""+nrOfTranscriptsPerPage);
		return getObject(docTarget, TrpDoc.class);
	}

	public void addPage(final int colId, final int docId, final int pageNr, File imgFile, IProgressMonitor monitor) {
		final WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(""+docId).path(""+pageNr).path(RESTConst.ADD_PATH);
		MultiPart mp = new MultiPart();
		mp.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
		
		if(monitor != null){
			BufferedFileBodyWriter bfbw = new BufferedFileBodyWriter();
			bfbw.addObserver(new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					if(arg instanceof Integer){
						monitor.worked((Integer)arg);
					}
				}});
			target.register(bfbw);
			
		}
		
		FileDataBodyPart imgPart = new FileDataBodyPart("img", imgFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		mp.bodyPart(imgPart);
		
		target.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA));
	}
	
	public void movePage(final int colId, final int docId, final int pageNr, final int toPageNr) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		final WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
				.path(""+colId).path(""+docId).path(""+pageNr).queryParam(RESTConst.MOVE_TO_PARAM, ""+toPageNr);
		super.postNull(target);		
	}
	
	public void deletePage(int colId, int docId, int pageNr) throws SessionExpiredException, IllegalArgumentException, ClientErrorException {
		final WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path("" + docId).path("" + pageNr);
		delete(docTarget);
	}

	public void checkSession() throws SessionExpiredException, ClientErrorException, ServerErrorException {
		final WebTarget target = baseTarget.path(RESTConst.AUTH_PATH).path(RESTConst.CHECK_SESSION);
		Response resp = target.request().get();
		checkStatus(resp, target);
	}
		
	public boolean isUserAllowedForJob(String jobImpl) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget t = baseTarget.path(RESTConst.USER_PATH)
										.path(RESTConst.IS_USER_ALLOWED_FOR_JOB_PATH)
										.queryParam(RESTConst.JOB_IMPL_PARAM, jobImpl);
		
		String isAllowed = getObject(t, String.class);
		return StringUtils.equals("true", isAllowed);
	}

	public TrpCrowdProject getCrowdProject(int colId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId).path(RESTConst.CROWD_PROJECT);
		return getObject(target, TrpCrowdProject.class);
	}

	/*
	 * Upload
	 */
	
	public TrpUpload createNewUpload(final int colId, Mets mets) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(colId < 1 || mets == null) {
			throw new IllegalArgumentException("bad parameters.");
		}
		WebTarget t = baseTarget.path(RESTConst.UPLOADS_PATH)
				.queryParam(RESTConst.COLLECTION_ID_PARAM, "" + colId);
		TrpUpload u = postEntityReturnObject(t, mets, MediaType.APPLICATION_XML_TYPE, 
				TrpUpload.class, MediaType.APPLICATION_JSON_TYPE);
		return u;
	}
	
	public TrpUpload createNewUpload(final int colId, DocumentUploadDescriptor struct) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(colId < 1 || struct == null) {
			throw new IllegalArgumentException("bad parameters.");
		}
		WebTarget t = baseTarget.path(RESTConst.UPLOADS_PATH)
				.queryParam(RESTConst.COLLECTION_ID_PARAM, "" + colId);
		TrpUpload u = postEntityReturnObject(t, struct, MediaType.APPLICATION_JSON_TYPE, 
				TrpUpload.class, MediaType.APPLICATION_JSON_TYPE);
		return u;
	}
	
	/**
	 * FIXME: filenames like "Action française_T00001.tiff" seem to get set correctly here but the server fails to interpret it and messes up the encoding.
	 * <br/>Maybe this might work? http://shchekoldin.com/2010/08/21/fix-for-jerseys-russian-files-names-bug/
	 * 
	 * @param uploadId
	 * @param img
	 * @param xml
	 * @return
	 * @throws SessionExpiredException
	 * @throws ClientErrorException
	 * @throws ServerErrorException
	 */
	public TrpUpload putPage(final int uploadId, File img, File xml) throws SessionExpiredException, ClientErrorException, ServerErrorException {
		if(uploadId < 1) {
			throw new IllegalArgumentException("No valid uploadId!");
		}
		WebTarget t = baseTarget.path(RESTConst.UPLOADS_PATH).path(""+uploadId);
		MultiPart mp = new MultiPart();
		mp.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
		FileDataBodyPart imgPart = new FileDataBodyPart("img", img, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		if(DEBUG) {
			FormDataContentDisposition cd = imgPart.getFormDataContentDisposition();
			logger.debug("\n\nPUT page");
			logger.debug("Name = " + cd.getName());	
			logger.debug("FileName = " + cd.getFileName());
			for(Entry<String, String> e : cd.getParameters().entrySet()) {
				logger.debug(e.getKey() + " = " + e.getValue());
			}
			logger.debug("\n\n");
		}
//		imgPart.setMediaType(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_TYPE.toString() + ";charset=utf-8"));
		mp.bodyPart(imgPart);
		
		if(xml != null) {
			FileDataBodyPart xmlPart = new FileDataBodyPart("xml", xml, MediaType.APPLICATION_OCTET_STREAM_TYPE);
//			xmlPart.setMediaType(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_TYPE.toString() + ";charset=utf-8"));
			mp.bodyPart(xmlPart);
		}
		return super.putEntityReturnObject(t, mp, MediaType.MULTIPART_FORM_DATA_TYPE, TrpUpload.class, MediaType.APPLICATION_JSON_TYPE);
	}

	public TrpUpload getUploadStatus(int uploadId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(uploadId < 1) {
			throw new IllegalArgumentException("No valid uploadId!");
		}
		WebTarget t = baseTarget.path(RESTConst.UPLOADS_PATH).path(""+uploadId);
		return super.getObject(t, TrpUpload.class, MediaType.APPLICATION_JSON_TYPE);
	}

	public void deleteUpload(int uploadId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(uploadId < 1) {
			throw new IllegalArgumentException("No valid uploadId!");
		}
		WebTarget t = baseTarget.path(RESTConst.UPLOADS_PATH).path(""+uploadId);
		super.delete(t);
	}

	public TrpUpload updateUploadMd(int uploadId, TrpDocMetadata md, Integer colId) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(uploadId < 1 || md == null) {
			throw new IllegalArgumentException("bad parameters.");
		}
		WebTarget t = baseTarget.path(RESTConst.UPLOADS_PATH).path(""+uploadId).path(RESTConst.MD_PATH);
		if(colId != null) {
			t = t.queryParam(RESTConst.COLLECTION_ID_PARAM, colId);
		}
		return postEntityReturnObject(t, md, MediaType.APPLICATION_JSON_TYPE, 
				TrpUpload.class, MediaType.APPLICATION_JSON_TYPE);
	}

	public String doCITlabKwsSearch(int colId, int docId, List<String> queries, KwsParameters params) throws SessionExpiredException, TrpServerErrorException, TrpClientErrorException { 
		if(queries == null || queries.isEmpty()) {
			throw new IllegalArgumentException("No queries given.");
		}
		WebTarget target = baseTarget.path(RESTConst.COLLECTION_PATH)
			.path(""+colId)
			.path(""+docId)
			.path(RESTConst.KWS_SEARCH_PATH);
		for(String q : queries){
			//encode the query, otherwise curly braces will be interpreted as parameter by Jersey
			final String encQ = UriComponent.encode(q,
                    UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
			target = target.queryParam(RESTConst.QUERY_PARAM, encQ);
		}	
		return postEntityReturnObject(target, params, MediaType.APPLICATION_JSON_TYPE, 
				String.class, MediaType.APPLICATION_XML_TYPE);
	}

	public TrpTotalTranscriptStatistics getCollectionStats(int colId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		final WebTarget docTarget = baseTarget.path(RESTConst.COLLECTION_PATH).path(""+colId)
				.path(RESTConst.STATS_PATH);
		return getObject(docTarget, TrpTotalTranscriptStatistics.class);
	}
	

}
