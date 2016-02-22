package eu.transkribus.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Observable;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.glassfish.jersey.message.internal.ReaderWriter;

@Provider
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class BufferedFileBodyWriter extends Observable implements MessageBodyWriter<File> {
	private static final Logger logger = LoggerFactory.getLogger(BufferedFileBodyWriter.class);
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return File.class.isAssignableFrom(type);
    }

    public void writeTo(File t, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        InputStream in = new FileInputStream(t);
        setChanged();
        notifyObservers("Adding: " + t.getName());
        try {
        	//100%
        	long size = t.length();
        	//1%
        	long onePercent = size/100;
        	//x%
        	long bytesCompleted = 0;
        	long oldPercentage = 0;
            int read;
            final byte[] data = new byte[ReaderWriter.BUFFER_SIZE];
//            logger.debug("Buffer size: " + ReaderWriter.BUFFER_SIZE);
            long dataWritten = 0;
            while ((read = in.read(data)) != -1) {
                entityStream.write(data, 0, read);
                dataWritten += ReaderWriter.BUFFER_SIZE;
                bytesCompleted += read;
                long newPercentage = bytesCompleted / onePercent;
                if(newPercentage != oldPercentage){// && newPercentage%5 == 0){
	                oldPercentage = newPercentage;
	                setChanged();
	                notifyObservers(Integer.valueOf(new Long(newPercentage).intValue()));
                }
 
                if(dataWritten >= 10000000){
                	logger.debug("Written " + dataWritten + " bytes");
                	logger.debug("Flushing!");
                	entityStream.flush();
                	dataWritten = 0;
                }
            }
        } finally {
            in.close();
        }
    }

    @Override
    public long getSize(File t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return t.length();
    }
}
