package eu.transkribus.client.connection;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import eu.transkribus.client.util.JerseyUtils;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpCreditPackage;
import eu.transkribus.core.model.beans.rest.TrpCreditPackageList;
import eu.transkribus.core.model.beans.rest.TrpCreditTransactionList;
import eu.transkribus.core.rest.RESTConst;

/**
 * API requests concerning credit-related object.
 * The endpoints are currently declared on different paths, i.e. collections, jobs and credits.
 * Methods here are moved to the specific file once the API structure is final.
 */
public class CreditCalls extends ApiResourcePath {

	CreditCalls(ATrpServerConn conn) {
		super(conn);
	}
	
	public TrpCreditPackage createCredit(TrpCreditPackage creditPackage) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget target = getBaseTarget()
				.path(RESTConst.CREDITS_PATH);
		return conn.postEntityReturnObject(target, creditPackage, TrpCreditPackage.class);
	}
	
	public TrpCreditPackageList getCreditPackagesByCollection(int colId, int index, int nValues, String sortField, String sortDirection) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget target = getBaseTarget()
				.path(RESTConst.COLLECTION_PATH)
				.path("" + colId)
				.path(RESTConst.CREDITS_PATH);
		target = JerseyUtils.setPagingParams(target, index, nValues, sortField, sortDirection);
		return conn.getObject(target, TrpCreditPackageList.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public TrpCreditPackage removeCreditPackageFromCollection(int colId, int packageId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget target = getBaseTarget()
				.path(RESTConst.COLLECTION_PATH)
				.path("" + colId)
				.path(RESTConst.CREDITS_PATH)
				.path("" + packageId);
		return conn.delete(target, TrpCreditPackage.class);
	}
	
	public TrpCreditPackage addCreditPackageToCollection(int colId, int packageId) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget target = getBaseTarget()
				.path(RESTConst.COLLECTION_PATH)
				.path("" + colId)
				.path(RESTConst.CREDITS_PATH)
				.path("" + packageId);
		return conn.postNullReturnObject(target, TrpCreditPackage.class);
	}
	
	public TrpCreditPackageList getCreditPackagesByUser(int index, int nValues, String sortField, String sortDirection) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget target = getBaseTarget()
				.path(RESTConst.CREDITS_PATH);
		target = JerseyUtils.setPagingParams(target, index, nValues, sortField, sortDirection);
		return conn.getObject(target, TrpCreditPackageList.class, MediaType.APPLICATION_XML_TYPE);
	}
	
	public TrpCreditTransactionList getTransactionsByJob(int jobId, int index, int nValues, String sortField, String sortDirection) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		WebTarget target = getBaseTarget()
				.path(RESTConst.JOBS_PATH)
				.path("" + jobId)
				.path(RESTConst.CREDIT_TRANSACTIONS_PATH);
		target = JerseyUtils.setPagingParams(target, index, nValues, sortField, sortDirection);
		return conn.getObject(target, TrpCreditTransactionList.class, MediaType.APPLICATION_XML_TYPE);
	}
}
