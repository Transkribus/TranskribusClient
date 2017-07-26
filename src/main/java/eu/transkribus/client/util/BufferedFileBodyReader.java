package eu.transkribus.client.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Observable;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class BufferedFileBodyReader extends Observable implements MessageBodyReader<File> {
	private final static Logger logger = LoggerFactory.getLogger(BufferedFileBodyReader.class);
	
	@Override public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		logger.debug("isReadable, type = "+type+" generictype = "+genericType+" annots = "+annotations+" mediaType = "+mediaType);
		return true;
	}

	@Override public File readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		logger.debug("type = "+type+" generictype = "+genericType
				+" annots = "+annotations+" mediaType = "+mediaType+" headers = "+httpHeaders+" stream = "+entityStream);
		
		
		
		
		return null;
	}
}
