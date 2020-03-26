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

public class PyLaiaCalls {
	
	ATrpServerConn conn;
	
	public PyLaiaCalls(ATrpServerConn conn) {
		this.conn = conn;
	}
	
	private WebTarget getBaseTarget() {
		return conn.baseTarget.path(RESTConst.PYLAIA_PATH);
	}
	
	public String runPyLaiaTraining(PyLaiaHtrTrainConfig config) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		if(config == null) {
			throw new IllegalArgumentException("Config is null!");
		}
		
		WebTarget target = getBaseTarget().path("" + config.getColId()).path(RESTConst.TRAIN_PATH);
		return conn.postEntityReturnObject(target, config, MediaType.APPLICATION_XML_TYPE, String.class, MediaType.APPLICATION_XML_TYPE);
	}	
	
	public String runPyLaiaHtrDecode(int colId, int docId, String pages, final int modelId, final String languageModel, 
			boolean doLinePolygonSimplification, boolean clearLines, boolean keepOriginalLinePolygons, boolean doWordSeg,
			int batchSize, List<String> structures) 
					throws SessionExpiredException, TrpServerErrorException, TrpClientErrorException {
		WebTarget target = getBaseTarget()
				.path(""+colId)
				.path(""+modelId)
				.path(RESTConst.RECOGNITION_PATH);
		target = target.queryParam(RESTConst.DOC_ID_PARAM, docId);
		target = target.queryParam(RESTConst.PAGES_PARAM, pages);
		target = target.queryParam(RESTConst.LANG_MOD_PARAM, languageModel);
		target = target.queryParam(JobConst.PROP_DO_LINE_POLYGON_SIMPLIFICATION, doLinePolygonSimplification);
		target = target.queryParam(JobConst.PROP_KEEP_ORIGINAL_LINE_POLYGONS, keepOriginalLinePolygons);
//		target = target.queryParam(JobConst.PROP_DO_STORE_CONFMATS, doStoreConfMats);
		target = target.queryParam(JobConst.PROP_CLEAR_LINES, clearLines);
		target = target.queryParam(JobConst.PROP_DO_WORD_SEG, doWordSeg);
		target = target.queryParam(JobConst.PROP_BATCH_SIZE, batchSize);
		if(!CollectionUtils.isEmpty(structures)) {
			target = target.queryParam(JobConst.PROP_STRUCTURES, new ArrayList<>(structures).toArray());
		}
		
		return conn.postEntityReturnObject(target, null, MediaType.APPLICATION_XML_TYPE, 
				String.class, MediaType.TEXT_PLAIN_TYPE);
	}

	public String runPyLaiaHtrDecode(int colId, DocumentSelectionDescriptor descriptor, final int modelId, final String languageModel,
			boolean doLinePolygonSimplification, boolean clearLines, boolean keepOriginalLinePolygons, boolean doWordSeg, int batchSize, List<String> structures)
					throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(descriptor == null || descriptor.getDocId() < 1) {
			throw new IllegalArgumentException("No document selected!");
		}
		WebTarget target = getBaseTarget()
				.path(""+colId)
				.path(""+modelId)
				.path(RESTConst.RECOGNITION_PATH);
		target = target.queryParam(RESTConst.LANG_MOD_PARAM, languageModel);
		target = target.queryParam(JobConst.PROP_DO_LINE_POLYGON_SIMPLIFICATION, doLinePolygonSimplification);
		target = target.queryParam(JobConst.PROP_KEEP_ORIGINAL_LINE_POLYGONS, keepOriginalLinePolygons);
//		target = target.queryParam(JobConst.PROP_DO_STORE_CONFMATS, doStoreConfMats);
		target = target.queryParam(JobConst.PROP_CLEAR_LINES, clearLines);
		target = target.queryParam(JobConst.PROP_DO_WORD_SEG, doWordSeg);
		target = target.queryParam(JobConst.PROP_BATCH_SIZE, batchSize);
		if(!CollectionUtils.isEmpty(structures)) {
			target = target.queryParam(JobConst.PROP_STRUCTURES, new ArrayList<>(structures).toArray());
		}
		return conn.postEntityReturnObject(target, descriptor, MediaType.APPLICATION_JSON_TYPE, 
				String.class, MediaType.TEXT_PLAIN_TYPE);
	}		

}
