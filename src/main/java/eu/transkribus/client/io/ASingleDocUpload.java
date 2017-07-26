package eu.transkribus.client.io;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.TrpDocPacker;
import eu.transkribus.core.model.beans.TrpDoc;

public abstract class ASingleDocUpload extends Observable implements Callable {
	private final static Logger logger = LoggerFactory.getLogger(ASingleDocUpload.class);
	
	protected IProgressMonitor monitor;
	protected Exception externalException=null;
	protected final String TEMP_DIR;
	protected TrpDoc doc;
	protected File zipFile;
	
	public ASingleDocUpload(TrpDoc doc, IProgressMonitor monitor) throws IOException {
		TEMP_DIR = System.getProperty("java.io.tmpdir");
		
		this.doc = doc;
		this.monitor = monitor;
		
		if (this.doc == null || this.doc.getMd() == null
				|| this.doc.getMd().getLocalFolder() == null) {
			throw new IOException("Document either has no metadata or is no localdocument!");
		}
	}
	
	protected boolean updateStatus(Object o) throws RuntimeException, OperationCanceledException {
		logger.debug("updateStatus: "+o);
		if (monitor != null) {
			if (externalException != null) {
				throw new RuntimeException(externalException);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException("Operation was canceled: "+o);
			}
			if (o instanceof String) {
				monitor.subTask((String) o);
			} else if (o instanceof Integer) {
				final Integer i = (Integer)o;
				String msg = "Uploading: " + i + "%";
				monitor.subTask(msg);
				monitor.worked(i);
				
				// test: cancel
//				if (i > 50)
//					monitor.setCanceled(true);
				
			}
		}
		
		setChanged();
		notifyObservers(o);
		return true;
	}
	
	protected void createZipFile(TrpDoc doc) throws IOException {
		updateStatus("Create zip file...");
		
		final String zipFilePath = TEMP_DIR + File.separator + "TRP_DOC_"
				+ System.currentTimeMillis() + ".zip";
//		final File zipFile;
		try {
			PassThroughObserver o = new PassThroughObserver();
			TrpDocPacker p = new TrpDocPacker();
			p.addObserver(o);
//			try {
//				conn.invalidate();
//			} catch (SessionExpiredException | ServerErrorException e) {
//				e.printStackTrace();
//			}				
			zipFile = p.packDocFiles(doc, zipFilePath);
		} catch (IOException e) {
			throw new IOException("Packaging document failed: "+e.getMessage());
		}

		updateStatus("Temp zip file at " + zipFile.getAbsoluteFile());
	}
	
	protected class PassThroughObserver implements Observer {
		public void update(Observable obj, Object arg) {
			if (arg instanceof String || arg instanceof Integer) {
				updateStatus(arg);
			}
		}
	}

}
