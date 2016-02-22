package eu.transkribus.client.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.mets.Mets;
import eu.transkribus.core.model.builder.mets.TrpMetsBuilder;
import eu.transkribus.core.util.ChecksumUtils;
import eu.transkribus.core.util.JaxbUtils;

/**
 * When the Entity reaches a size of MAX_INTEGER-1 bytes it will run into a Jersey internal limit
 *
 */
@Deprecated
public class TrpDocUploadMultipart extends Observable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TrpDocUploadMultipart.class);
	protected final String TEMP_DIR;
	protected final WebTarget target;
	protected final TrpDoc doc;
	public Throwable error=null;
	
	public TrpDocUploadMultipart(WebTarget target, TrpDoc entity) throws IOException {
//		super(target, entity);
		TEMP_DIR = System.getProperty("java.io.tmpdir");
		this.target = target;
		this.doc = entity;
		if (this.doc == null || this.doc.getMd() == null
				|| this.doc.getMd().getLocalFolder() == null) {
			throw new IOException("Document either has no metadata or is no localdocument!");
		}
	}

	@Override
	public void run() {
//		PassThroughObserver o = new PassThroughObserver();
		updateStatus("Running process...");

		File metsFile;
		try{
			for (TrpPage p : doc.getPages()) {
				p.setMd5Sum(computeChkSum(p.getUrl()));
				for(TrpTranscriptMetadata t : p.getTranscripts()){
					t.setMd5Sum(computeChkSum(t.getUrl()));
				}
			}
			String metsFilePath = doc.getMd().getLocalFolder().getAbsoluteFile() + File.separator + TrpMetsBuilder.METS_FILE_NAME;
			metsFile = new File(metsFilePath);
			Mets mets;
			logger.info("Creating METS file at: " + metsFilePath);
			//build a mets that points to all files we need
			//2nd arg: export page files (add to mets filesec), 3rd arg: export alto files
			mets = TrpMetsBuilder.buildMets(doc, true, false);
			
			try {
				metsFile = JaxbUtils.marshalToFile(mets, metsFile, TrpDocMetadata.class);
			} catch (JAXBException e) {
				logger.error(e.getMessage(), e);
				throw new IOException("Could not create METS file.", e);
			}
		} catch (IOException e){
			logger.error(e.getMessage(), e);
			return;
		}
		
		MultiPart mp = new MultiPart();
		mp.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
		
		FileDataBodyPart metsPart = new FileDataBodyPart("mets", metsFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		mp.bodyPart(metsPart);
		
		for(TrpPage p : doc.getPages()){
			File img = null;

			img = new File(p.getUrl().getPath());

			FileDataBodyPart imgPart = new FileDataBodyPart("img", img, MediaType.APPLICATION_OCTET_STREAM_TYPE);
	
			TrpTranscriptMetadata tmd = p.getCurrentTranscript();
			File xml = new File(tmd.getUrl().getPath());
			
			FileDataBodyPart xmlPart = new FileDataBodyPart("xml", xml, MediaType.APPLICATION_OCTET_STREAM_TYPE);
			
			mp.bodyPart(imgPart);
			mp.bodyPart(xmlPart);
		}
		
		updateStatus("Starting upload...");
		
//		BufferedFileBodyWriter bfbw = new BufferedFileBodyWriter();
//		bfbw.addObserver(o);

//		target.register(bfbw);
		Response response = target.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA));
		updateStatus("Request sent: " + response.getStatusInfo().toString());
	}
	
	private String computeChkSum(URL url) throws IOException{
		if(!url.getProtocol().contains("file")){
			throw new IOException(url + " is not a local file!");
		}
		final String chkSum;
		final String path = url.getPath();
		File f = new File(path);
		updateStatus("Computing checksum: " + f.getAbsolutePath());
		chkSum = ChecksumUtils.getMd5SumHex(f);
		return chkSum;
	}
	
	protected void updateStatus(String string) {
		setChanged();
		notifyObservers(string);
	}
	
	protected class PassThroughObserver implements Observer {
		public void update(Observable obj, Object arg) {
			if (arg instanceof String) {
				updateStatus((String) arg);
			}
		}
	}
}
