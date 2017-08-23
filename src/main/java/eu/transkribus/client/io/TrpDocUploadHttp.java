package eu.transkribus.client.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.io.util.Md5SumComputer;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.DocumentUploadDescriptor;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpUpload;
import eu.transkribus.core.model.beans.TrpUpload.UploadType;
import eu.transkribus.core.model.beans.mets.Mets;
import eu.transkribus.core.model.builder.TrpDocUploadBuilder;
import eu.transkribus.core.model.builder.mets.TrpMetsBuilder;

/**
 * New upload that sends a separate PUT request per page<br/><br/>
 * 
 * TODO-list
 *  <ul>
 *  	<li>do retries if a single page upload fails</li>
 *  	<li>cache upload processes locally (on disk?) in order to allow resuming if application crashed</li>
 *  </ul>
 * 
 */
public class TrpDocUploadHttp extends ASingleDocUpload {
	private static final Logger logger = LoggerFactory.getLogger(TrpDocUploadHttp.class);
	protected final TrpServerConn conn;
	IProgressMonitor monitor = null;
	final int colId;
	final UploadType type;
	final boolean doMd5SumCheck;

	public TrpDocUploadHttp(TrpServerConn conn, final int colId, final TrpDoc entity, final UploadType type,
			final boolean doMd5SumCheck, IProgressMonitor monitor) throws IOException {
		super(entity, monitor);
		if (conn == null) {
			throw new IllegalArgumentException("TrpServer connection is null.");
		}
		this.conn = conn;
		this.colId = colId;
		this.type = type == null ? UploadType.METS : type;
		this.doMd5SumCheck = doMd5SumCheck;
	}

	@Override
	public TrpUpload call() throws Exception {

		TrpUpload upload = null;
		try {
			if (monitor != null)
				monitor.beginTask("Uploading document at " + doc.getMd().getLocalFolder().getAbsolutePath(),
						100);

			if (doMd5SumCheck) {
				Md5SumComputer md5Comp = new Md5SumComputer();
				md5Comp.addObserver(passthroughObserver);
				doc = md5Comp.computeAndSetMd5Sums(doc);
			}

			updateStatus("Initiating upload...");
			
			final int uploadId;
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
			uploadId = upload.getUploadId();
			
//			if(true) return null;
			
			// put files
			final int percentPerPage = 100 / doc.getNPages();
			for(TrpPage p : doc.getPages()) {
				File img = FileUtils.toFile(p.getUrl());
				File xml = null;
				if(!p.getTranscripts().isEmpty()) {
					xml = FileUtils.toFile(p.getCurrentTranscript().getUrl());
				}
				
				//TODO retry loop if putPage fails
				upload = conn.putPage(uploadId, img, xml);
				updateStatus(Integer.valueOf(percentPerPage * p.getPageNr()));
			}
			
			updateStatus("Upload done. Waiting for server...");
			while(upload.getJobId() == null) {
				Thread.sleep(3000);
				//poll service for jobId
				upload = conn.getUploadStatus(uploadId);
				if(upload.isUploadComplete()) {
					updateStatus("Server is starting ingest job...");
				}
			}
			
		} catch (OperationCanceledException oce) {
			logger.info("upload canceled: " + oce.getMessage());
		} catch (Exception e) {
			logger.error("Upload failed!", e);
		}

		return upload;
	}
}
