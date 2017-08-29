package eu.transkribus.client.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.io.LocalDocConst;
import eu.transkribus.core.io.util.Md5SumComputer;
import eu.transkribus.core.model.beans.DocumentUploadDescriptor;
import eu.transkribus.core.model.beans.DocumentUploadDescriptor.PageUploadDescriptor;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpUpload;
import eu.transkribus.core.model.beans.TrpUpload.UploadType;
import eu.transkribus.core.model.beans.mets.Mets;
import eu.transkribus.core.model.builder.TrpDocUploadBuilder;
import eu.transkribus.core.model.builder.mets.TrpMetsBuilder;
import eu.transkribus.core.util.JaxbUtils;

/**
 * New upload that sends a separate PUT request per page<br/>
 * In case something goes wrong or upload is canceled the upload object will be stored on disk in
 * the local folder of the document. On a subsequent upload attempt, the file will be picked up
 * and the upload is resumed. 
 */
public class TrpDocUploadHttp extends ASingleDocUpload {
	private static final Logger logger = LoggerFactory.getLogger(TrpDocUploadHttp.class);
	private static final String UPLOAD_XML_NAME = "upload.xml";
	private static final boolean DEBUG = false;
	
	/**
	 * If a single page upload fails there will be this many retries before the complete upload fails
	 */
	private static final int NR_OF_RETRIES_ON_FAIL = 3;
	private static final UploadType DEFAULT_TYPE = UploadType.JSON;
	protected final TrpServerConn conn;
	IProgressMonitor monitor = null;
	final int colId;
	final UploadType type;
	final boolean doMd5SumCheck;

	/**
	 * @param conn a logged in connection object
	 * @param colId the collection where the document is uploaded to
	 * @param entity local TrpDoc object
	 * @param type specifies the object type that is used to send the doc's structure (JSON | METS)
	 * @param doMd5SumCheck whether to compute and send MD5 sums to check file transmission on the server. recommended!
	 * @param monitor IProgressMonitor to show upload process
	 * @throws IOException if document entity is null, has no metadata or is not a local document
	 * @throws IllegalArgumentExcpetion if the connection is null or not logged in
	 */
	public TrpDocUploadHttp(TrpServerConn conn, final int colId, final TrpDoc entity, final UploadType type,
			final boolean doMd5SumCheck, IProgressMonitor monitor) throws IOException, IllegalArgumentException {
		super(entity, monitor);
		if (conn == null) {
			throw new IllegalArgumentException("TrpServer connection is null.");
		}
		if(conn.getUserLogin() == null) {
			throw new IllegalArgumentException("You are not logged in.");
		}
		this.conn = conn;
		this.colId = colId;
		//default is JSON
		this.type = type == null ? DEFAULT_TYPE : type;
		this.doMd5SumCheck = doMd5SumCheck;
		
		this.conn.enableDebugLogging(DEBUG);
	}

	@Override
	public TrpUpload call() throws Exception {

		if (monitor != null) {
			monitor.beginTask("Initiating upload of document at " + doc.getMd().getLocalFolder().getAbsolutePath(),
					100);
		}
		
		TrpUpload upload = null;
		try {
			//set checksums. those are necessary to check if files have changed when resuming an upload
			if(!doc.hasChecksumsSet() && doMd5SumCheck) {
				computeAndSetChecksum(doc);
			}
			
			
			//check for existence of upload.xml on local folder of doc and check if upload is recoverable
			upload = initUploadObject(doc);

			updateStatus("Initiating upload...");
			
			//the object is not null only if upload is resumable
			if(upload == null) {
				upload = createNewUpload();
			}
			final float percentPerPage = 100f / doc.getNPages();
			final File sourceDir = doc.getMd().getLocalFolder();
			final File pageSourceDir = new File(sourceDir.getAbsolutePath() 
					+ File.separator + LocalDocConst.PAGE_FILE_SUB_FOLDER);
			// put files	
			for(PageUploadDescriptor p : upload.getPages()) {
				if(!p.isPageUploaded()) {
					uploadPage(upload, p, sourceDir, pageSourceDir);
				}
				logger.debug("Page nr.: " + p.getPageNr() + " | percentPerPage = " + percentPerPage);
				final int percent = new Double(percentPerPage * p.getPageNr()).intValue();
				updateStatus(Integer.valueOf(percent));
			}
			
			// the last PUT request's response includes the job ID
			logger.info("Ingest-job ID = " + upload.getJobId());
			
			//if there is no job ID now there is something gone wrong
			if(upload.isUploadComplete() && upload.getJobId() == null) {
				String msg = "Upload with ID = " + upload.getUploadId() + " failed for user: " 
						+ upload.getUserId() + " -> " + upload.getUserName() +"\n\n";
				msg += JaxbUtils.marshalToString(upload, TrpDocMetadata.class, PageUploadDescriptor.class);
				conn.sendBugReport("bugs@transkribus.eu", "Upload failed!", msg, true, true, null);
				throw new IllegalStateException("Upload could not be completed! A bug report has been sent and we are investigating the problem.");
			}
			
		} catch (OperationCanceledException oce) {
			logger.info("Upload canceled: " + oce.getMessage());
			storeUploadXmlOnDisk(upload, doc.getMd().getLocalFolder());
		} catch (Exception e) {
			logger.error("Upload failed!", e);
			storeUploadXmlOnDisk(upload, doc.getMd().getLocalFolder());
			throw e;
		}

		return upload;
	}
	
	private void computeAndSetChecksum(TrpDoc doc) throws IOException {
		updateStatus("Computing checksums...");
		Md5SumComputer md5Comp = new Md5SumComputer();
		md5Comp.addObserver(passthroughObserver);
		doc = md5Comp.computeAndSetMd5Sums(doc);
	}

	/**
	 * Uploads a single page, i.e. the image and optionally the PAGE XML, as specified in the structure.
	 * If Upload fails the method will retry {@value #NR_OF_RETRIES_ON_FAIL} times.
	 * @param upload
	 * @param p
	 * @param sourceDir
	 * @param pageSourceDir
	 * @throws Exception
	 */
	private void uploadPage(TrpUpload upload, PageUploadDescriptor p, final File sourceDir, final File pageSourceDir) throws Exception {
		File img = new File(sourceDir.getAbsolutePath() + File.separator + p.getFileName());
		File xml = null;
		if(!StringUtils.isEmpty(p.getPageXmlName())) {
			xml = new File(pageSourceDir.getAbsolutePath() + File.separator + p.getPageXmlName());
		}
		//retry loop if putPage() fails
		int tries = 0;
		Exception ex;
		do {
			try {
				upload = conn.putPage(upload.getUploadId(), img, xml);
				ex = null;
			} catch(Exception e) {
				logger.error("Could not post image: " + img.getName(), e);
				ex = e;
			}
		} while (tries++ <= NR_OF_RETRIES_ON_FAIL && ex != null);
		if(ex != null) {
			throw ex;
		}
		p.setPageUploaded(true);
	}

	private TrpUpload createNewUpload() throws SessionExpiredException, ServerErrorException, ClientErrorException, IOException {		
		TrpUpload upload;
		switch (type) {
		case METS:
			Mets mets = TrpMetsBuilder.buildMets(doc, true, false, true, null);
			upload = conn.createNewUpload(colId, mets);
			break;
		case JSON:
			DocumentUploadDescriptor struct = TrpDocUploadBuilder.build(doc);
			upload = conn.createNewUpload(colId, struct);
			break;
		case NoStructure:
			throw new NotImplementedException();
		default:
			throw new IllegalArgumentException("type is null.");	
		}
		return upload;
	}

	/**
	 * Check for existence of upload XML from a failed/canceled upload in source dir.
	 * If one is found:
	 * <ul>
	 * <li>check if user ID matches (otherwise user is not allowed to continue)</li>
	 * <li>check if directory content has changed. If yes delete upload on server as it is not recoverable.</li>
	 * <li>if everything is fine update the upload's docMd on server just in case</li>
	 * </ul>
	 * Method returns not null only if upload is recoverable. 
	 * @param sourceDir
	 * @return
	 * @throws SessionExpiredException
	 * @throws ServerErrorException
	 * @throws ClientErrorException
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	private TrpUpload initUploadObject(TrpDoc doc) throws SessionExpiredException, ServerErrorException, ClientErrorException, FileNotFoundException, JAXBException {
		TrpUpload upload = null;
		File uploadXml = new File(doc.getMd().getLocalFolder().getAbsolutePath() + File.separator + UPLOAD_XML_NAME);
		if(uploadXml.isFile()) {
			upload = JaxbUtils.unmarshal(uploadXml, TrpUpload.class, TrpDocMetadata.class, PageUploadDescriptor.class);
			//check if user ID matches before! (might be NAS storage).
			if(conn.getUserLogin().getUserId() != upload.getUserId()) {
				logger.debug("upload.xml was written by a different user.");
				upload = null;
			} else {
				//refresh state from server
				try {
					upload = conn.getUploadStatus(upload.getUploadId());
				} catch (ServerErrorException | ClientErrorException e) {
					logger.error("Could not get upload object from server. Upload has to be restarted.", e);
					return null;
				}
				//check if files in sourceDir are now different
				DocumentUploadDescriptor struct = TrpDocUploadBuilder.build(doc);
				
				if(!struct.equals((DocumentUploadDescriptor)upload)) {
					//remove old upload on server as it is not recoverable
					logger.debug("Upload is not recoverable as source dir content has changed!");
					conn.deleteUpload(upload.getUploadId());
					upload = null;
				} else {
					logger.debug("Upload is recoverable. Update DocMd on server.");
					
					//check if this is the same collection and update if necessary
					Integer colId = null;
					if(this.colId != upload.getColId()) {
						colId = this.colId;
						logger.debug("Upload collection has changed to ID = " + colId);
					}
					//update doc md and collection ID on server in case something has changed
					upload = conn.updateUploadMd(upload.getUploadId(), upload.getMd(), colId);
				}
			}
		}
		return upload;		
	}

	/**
	 * Stores an upload object as {@value #UPLOAD_XML_NAME} in the localFolder specified
	 * 
	 * @param upload
	 * @param localFolder
	 */
	private void storeUploadXmlOnDisk(TrpUpload upload, File localFolder) {
		if(upload == null) {
			return;
		}
		File xml = new File(localFolder.getAbsolutePath() + File.separator + UPLOAD_XML_NAME);
		try {
			JaxbUtils.marshalToFile(upload, xml, TrpDocMetadata.class, PageUploadDescriptor.class);
		} catch (FileNotFoundException | JAXBException e) {
			logger.error("Could not store upload.xml!", e);
		}
	}
}
