package eu.transkribus.client.connection;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.PyLaiaHtrTrainConfig;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.rest.RESTConst;

public class DocUnderstandingCalls {

    ATrpServerConn conn;
	
	public DocUnderstandingCalls(ATrpServerConn conn) {
		this.conn = conn;
    }
    
    private WebTarget getBaseTarget() {
		return conn.baseTarget.path(RESTConst.DU_PATH);
    }
    
    public String runDocUnderstandingDecode(int colId,
        int docId,
        String pages,
        final int modelId) throws SessionExpiredException, TrpServerErrorException, TrpClientErrorException {

        WebTarget target = getBaseTarget()
                .path(""+colId)
                .path(""+modelId)
                .path(RESTConst.RECOGNITION_PATH);

        target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
        target = target.queryParam(RESTConst.PAGES_PARAM, pages);

        return conn.postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
                String.class, MediaType.TEXT_PLAIN_TYPE);
        }
    
}