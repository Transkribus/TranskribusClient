package eu.transkribus.client.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ParseException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.ProgressInputStream;
import eu.transkribus.core.util.ProgressInputStream.ProgressInputStreamListener;

public class JerseyUtils {
	private final static Logger logger = LoggerFactory.getLogger(JerseyUtils.class);
	
	public static String getFilenameFromContentDisposition(Response resp) throws ParseException {
		/*
		 * FIXME javax.mail can be removed from TranskribusCore dependencies when this type is replaced
		 */
		ContentDisposition cd = new ContentDisposition(resp.getHeaderString("Content-Disposition"));
		return cd.getParameter("filename");
	}

	public static Long getContentLengthHeader(Response resp) {
		String cl = resp.getHeaderString("Content-Length");
		if (cl == null) {
			logger.debug("No Content-Length header found");
			return null;
		}
		
		try {
			return Long.valueOf((String) cl);
		} catch (NumberFormatException e) {
			logger.warn("could not parse content length: "+cl);
			return null;
		}
	}
	
	public static void downloadFile(Response resp, ProgressInputStreamListener l, File f) throws FileNotFoundException, IOException {
		Long cl = JerseyUtils.getContentLengthHeader(resp);
		if (cl == null) {
			cl = 0L;
		}
		ProgressInputStream input = new ProgressInputStream((InputStream) resp.getEntity(), cl);
		if (l != null)
			input.addProgressInputStreamListener(l);
				
		IOUtils.copy(input, new FileOutputStream(f));
	}
	
	public static WebTarget queryParam(WebTarget t, String param, Collection<?> c) {
		if (c != null) {
			for (Object o : c) {
				t = t.queryParam(param, o);	
			}
		}
	
		return t;
	}
	
	public static WebTarget queryParam(WebTarget t, String param, Object o) {
		if ( o == null || (o instanceof String && ((String)o).isEmpty()) ) {
			return t;
		}
		if (o instanceof Iterable<?>) { // needed?? -> function with Iterable type parameter should be called anyway...
			return queryParam(t, param, (Iterable<?>) o);
		}
		if (o instanceof Collection<?>) { // needed?? -> function with Collection type parameter should be called anyway...
			return queryParam(t, param, (Collection<?>) o);
		}
		else return t.queryParam(param, o);
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

}
