package eu.transkribus.client.io;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.misc.APassthroughObservable;
import eu.transkribus.core.model.beans.TrpDoc;

public abstract class ASingleDocUpload extends APassthroughObservable implements Callable<Object> {
	private final static Logger logger = LoggerFactory.getLogger(ASingleDocUpload.class);
	
	protected IProgressMonitor monitor;
	protected TrpDoc doc;

	public ASingleDocUpload(TrpDoc doc, IProgressMonitor monitor) throws IOException {
		super();
		this.doc = doc;
		this.monitor = monitor;
		if (this.doc == null || this.doc.getMd() == null
				|| this.doc.getMd().getLocalFolder() == null) {
			throw new IOException("Document either has no metadata or is no localdocument!");
		}
		final String errors = doc.getImageErrors();
		if(!StringUtils.isEmpty(errors)) {
			throw new IOException("The document includes faulty images! "
					+ "Please resolve the issues and try again.\n" + errors);
		}
	}
	
	@Override
	protected void updateStatus(Object o) throws RuntimeException, OperationCanceledException {
		logger.debug("updateStatus: "+o);
		if (monitor != null) {
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
		super.updateStatus(o);
	}
}
