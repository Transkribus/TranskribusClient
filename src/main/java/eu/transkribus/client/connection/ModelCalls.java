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
import eu.transkribus.core.model.beans.ATrpModel;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpP2PaLA;
import eu.transkribus.core.rest.RESTConst;
import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.core.util.ModelUtil;

public class ModelCalls {
	private static final Logger logger = LoggerFactory.getLogger(ModelCalls.class);
	
	ATrpServerConn conn;
	
	public ModelCalls(ATrpServerConn conn) {
		this.conn = conn;
	}
	
	private WebTarget getBaseModelTarget() {
		return conn.baseTarget.path(RESTConst.MODELS_PATH);
	}
	
	public <T extends ATrpModel> T updateModel(T model, Class<T> clazz) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = getBaseModelTarget();
		
//		conn.postEntity(t, model, MediaType.APPLICATION_JSON_TYPE);
		return conn.postEntityReturnObject(t, model, MediaType.APPLICATION_JSON_TYPE, clazz, MediaType.APPLICATION_JSON_TYPE);
	}
	
	public TrpP2PaLA getP2PaLAModel(int modelId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return getModel(modelId, TrpP2PaLA.class);
	}
	
	public <T extends ATrpModel> T getModel(int modelId, Class<T> clazz/*, String type*/) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = getBaseModelTarget().path(""+modelId);//.path(type);
		
		return (T) conn.getObject(t, clazz);
	}
	
	public void setModelDeleted(int modelId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = getBaseModelTarget().path(""+modelId);
		
		conn.delete(t);
	}
	
	public List<TrpP2PaLA> getP2PaLAModels(boolean onlyActive, boolean allModels, Integer colId, Integer userId, Integer releaseLevel) throws SessionExpiredException {
		return getModels(onlyActive, allModels, colId, userId, releaseLevel, TrpP2PaLA.TYPE);
	}
	
	public <T extends ATrpModel> List<T> getModels(boolean onlyActive, boolean allModels, Integer colId, Integer userId, Integer releaseLevel, Class<T> clazz) throws SessionExpiredException {
		return (List<T>) getModels(onlyActive, allModels, colId, userId, releaseLevel, ModelUtil.getType(clazz));		
	}
	
	public <T extends ATrpModel> List<T> getModels(boolean onlyActive, boolean allModels, Integer colId, Integer userId, Integer releaseLevel, String type) throws SessionExpiredException {
		WebTarget t = getBaseModelTarget().path(RESTConst.LIST_PATH);
		
		t = JerseyUtils.queryParam(t, RESTConst.TYPE_PARAM, type);
		t = JerseyUtils.queryParam(t, RESTConst.ONLY_ACTIVE_PARAM, ""+onlyActive);
		t = JerseyUtils.queryParam(t, RESTConst.ALL_PARAM, ""+allModels);
		t = JerseyUtils.queryParam(t, RESTConst.COLLECTION_ID_PARAM, colId);
		t = JerseyUtils.queryParam(t, RESTConst.USER_ID_PARAM, userId);
		t = JerseyUtils.queryParam(t, RESTConst.RELEASE_LEVEL_PARAM, releaseLevel);
		
//		return conn.getList(t, new GenericType<List<T>>(){});
		return conn.getList(t, ModelUtil.createGenericType(type));
	}
	
	// Model <-> Collections stuff
	
	public List<Integer> getModelCollectionIds(int modelId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = conn.baseTarget.path(RESTConst.MODELS_PATH).path(""+modelId).path(RESTConst.COLLECTION_PATH).path(RESTConst.LIST_PATH);
		t = t.queryParam(RESTConst.TRANSCRIPT_IDS_PARAM, true);
		String responseStr = conn.getObject(t, String.class, MediaType.APPLICATION_JSON_TYPE);
		return GsonUtil.toIntegerList(responseStr);		
	}
	
	public List<TrpCollection> getModelCollections(int modelId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget t = conn.baseTarget.path(RESTConst.MODELS_PATH).path(""+modelId).path(RESTConst.COLLECTION_PATH).path(RESTConst.LIST_PATH);
		return conn.getList(t, TrpServerConn.COL_LIST_TYPE);
	}
	
	public void addOrRemoveModelFromCollection(int modelId, int colId, boolean delete) throws TrpClientErrorException, TrpServerErrorException, SessionExpiredException {
		WebTarget t = conn.baseTarget.path(RESTConst.MODELS_PATH).path(""+modelId).path(RESTConst.COLLECTION_PATH).path(""+colId);
		
		if (!delete) {
			conn.postNull(t);
		}
		else {
			conn.delete(t);
		}
	}

}
