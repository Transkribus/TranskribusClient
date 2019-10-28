package eu.transkribus.client.connection;

import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.JerseyUtils;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.rest.RESTConst;
import eu.transkribus.core.util.GsonUtil;

public class ModelCalls {
	private static final Logger logger = LoggerFactory.getLogger(ModelCalls.class);
	
	ATrpServerConn conn;
	
	public ModelCalls(ATrpServerConn conn) {
		this.conn = conn;
	}
	
	public List<Integer> getModelCollectionIds(int modelId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = conn.baseTarget.path(RESTConst.MODELS_PATH).path(""+modelId).path(RESTConst.COLLECTION_ID_PARAM).path(RESTConst.LIST_PATH);
		String responseStr = conn.getObject(t, String.class, MediaType.TEXT_PLAIN_TYPE);
		return GsonUtil.toIntegerList(responseStr);		
	}
	
	public List<TrpCollection> getModelCollections(int modelId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = conn.baseTarget.path(RESTConst.MODELS_PATH).path(""+modelId).path(RESTConst.COLLECTION_PARAM).path(RESTConst.LIST_PATH);
		return conn.getList(t, TrpServerConn.COL_LIST_TYPE);
	}
	
	public void addOrRemoveModelFromCollection(int modelId, int colId, boolean delete) throws TrpClientErrorException, TrpServerErrorException, SessionExpiredException {
		WebTarget t = conn.baseTarget.path(RESTConst.MODELS_PATH).path(""+modelId).path(""+colId);
		
		if (!delete) {
			conn.postNull(t);
		}
		else {
			conn.delete(t);
		}
	}

}
