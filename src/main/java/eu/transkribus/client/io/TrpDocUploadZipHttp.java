package eu.transkribus.client.io;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.BufferedFileBodyWriter;
import eu.transkribus.core.io.TrpDocPacker;
import eu.transkribus.core.model.beans.TrpDoc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Zip files with more than MAX_INTEGER-1 bytes of content will run into Jersey internal file size limit.
 */
//@Deprecated
public class TrpDocUploadZipHttp extends ASingleDocUpload {
	private static final Logger logger = LoggerFactory.getLogger(TrpDocUploadZipHttp.class);
//	protected final String TEMP_DIR;
	protected final WebTarget target;
//	protected final TrpDoc doc;
	public Throwable error=null;
	IProgressMonitor monitor=null;
	
	public static final int MAX_UPLOAD_SIZE_MB = 500; // max size in MB

	public TrpDocUploadZipHttp(WebTarget target, TrpDoc entity, IProgressMonitor monitor) throws IOException {
		super(entity, monitor);
		
		this.target = target;
	}

	@Override
	public Object call() throws Exception {
		
		try {
			if (monitor != null)
				monitor.beginTask("Uploading document at "+doc.getMd().getLocalFolder().getAbsolutePath(), 100);
			
			PassThroughObserver o = new PassThroughObserver();
			updateStatus("Running process...");
	
			// create zip file:
			createZipFile(doc);
	
			//post doc to target
			logger.info("Posting file of size: " + zipFile.length());
			
			if (zipFile.length() > (MAX_UPLOAD_SIZE_MB * 1024 * 1024) ) {
				throw new IOException("The size of the zip file to upload is too large - try the ftp ingest instead!");
			}
			
			updateStatus("Starting upload...");
			
			Entity<File> ent = Entity.entity(zipFile, MediaType.APPLICATION_OCTET_STREAM);
	
			final String sContentDisposition = "attachment; " + "filename=\"" + zipFile.getName()
					+ "\"; " + "size=" + zipFile.length();
	
			BufferedFileBodyWriter bfbw = new BufferedFileBodyWriter();
			bfbw.addObserver(o);
			
			target.register(bfbw);
			
			Response response = target.request().header("Content-Disposition", sContentDisposition).post(ent);
			updateStatus("Request sent. " + response.getStatusInfo().toString());
			
	//		updateStatus("Document uploaded - starting to process file on server");
			//remove the zip file
			boolean isDeleted = zipFile.delete();
			updateStatus("Zip file " + (isDeleted ? "successfully" : "could not be") + " deleted.");
		}
		catch (javax.ws.rs.ProcessingException e) {
			if (e.getCause() instanceof OperationCanceledException) {
				logger.info("1 upload canceled: "+e.getMessage());
			} else
				throw e;
		}
		catch (OperationCanceledException e) {
			logger.info("upload canceled: "+e.getMessage());
		}
		
		return null;
	}

//	protected void updateStatus(String string) {
//		setChanged();
//		notifyObservers(string);
//	}
	
//	protected class PassThroughObserver implements Observer {
//		public void update(Observable obj, Object arg) {
//			if (arg instanceof String) {
//				updateStatus((String) arg);
//			}
//		}
//	}
}
