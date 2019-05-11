package hds.server.controllers;

import hds.security.exceptions.SignatureException;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.OwnerDataMessage;
import hds.security.msgtypes.WriteResponse;
import hds.server.ServerApplication;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.MarkForSale;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.DateUtils.isFreshTimestamp;
import static hds.security.SecurityManager.verifyWriteOnGoodsOperationSignature;
import static hds.server.controllers.controllerHelpers.GeneralControllerHelper.incrementClientTimestamp;
import static hds.server.controllers.controllerHelpers.GeneralControllerHelper.isFreshLogicTimestamp;
import static hds.server.helpers.TransactionValidityChecker.getCurrentOwner;
import static hds.server.helpers.TransactionValidityChecker.isClientWilling;

/**
 * Responsible for handling POST requests for the endpoint /intentionToSell.
 * Confirms authenticity and integrity of the request.
 * Marks a GoodID for sale in the database.
 *
 * @author 		Rafael Ribeiro
 * @see 		OwnerDataMessage
 */
@SuppressWarnings("Duplicates")
@RestController
public class IntentionToSellController {
	private static final String FROM_SERVER = ServerApplication.getPort();
	private static final String OPERATION = "markForSale";

	/**
	 * REST Controller responsible for marking a goodID for sale.
	 *
	 * @param 	ownerData 		GoodID and SellerID
	 * @param 	result    		result of validators for inputs of ownerData
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see		OwnerDataMessage
	 * @see 	BindingResult
	 */
	@PostMapping(value = "/intentionToSell",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> intentionToSell(@RequestBody @Valid OwnerDataMessage ownerData, BindingResult result) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Intention to Sell request.");
		logger.info("\tRequest: " + ownerData.toString());

		UserRequestIDKey key = new UserRequestIDKey(ownerData.getFrom(), ownerData.getRequestID());
		ResponseEntity<BasicMessage> cachedResponse = GeneralControllerHelper.tryGetRecentRequest(key);
		if (cachedResponse != null) {
			return cachedResponse;
		}

		MetaResponse metaResponse;

		// TODO - Add this to custom validation. //
		long timestamp = ownerData.getTimestamp();
		if (!isFreshTimestamp(timestamp)) {
			String reason = "Timestamp " + timestamp + " is too old";
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getTo(), "", ControllerErrorConsts.OLD_MESSAGE, reason);
			metaResponse = new MetaResponse(408, payload);
			return GeneralControllerHelper.getResponseEntity(metaResponse, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		}

		if(result.hasErrors()) {
			metaResponse = GeneralControllerHelper.handleInputValidationResults(result, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
			ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
			GeneralControllerHelper.cacheRecentRequest(key, response);
			return response;
		}

		String clientID = ownerData.getOwner();
		// TODO - Add this to custom validation. //
		long wts = ownerData.getWriteTimestamp();
		if (!isFreshLogicTimestamp(clientID, wts)) {
			String reason = "write timestamp " + wts + " is too old";
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getTo(), "", ControllerErrorConsts.OLD_MESSAGE, reason);
			metaResponse = new MetaResponse(408, payload);
			return GeneralControllerHelper.getResponseEntity(metaResponse, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		}
		incrementClientTimestamp(clientID);

		try {
			metaResponse = execute(ownerData);
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		}
		ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		GeneralControllerHelper.cacheRecentRequest(key, response);
		return response;
	}

	/**
	 * Confirms authenticity and integrity of the request.
	 * Marks a GoodID for sale in the database.
	 *
	 * @param 	ownerData 			GoodID and SellerID
	 * @return 	MetaResponse 		Contains an HttpStatus code and a BasicMessage
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @see 	OwnerDataMessage
	 * @see 	MetaResponse
	 */
	private MetaResponse execute(OwnerDataMessage ownerData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		String sellerID = InputValidation.cleanString(ownerData.getOwner());
		String goodID = InputValidation.cleanString(ownerData.getGoodID());
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);
			String ownerID = getCurrentOwner(conn, goodID);
			if (!ownerID.equals(sellerID)) {
				// TODO - Rollback is not needed here. //
				conn.rollback();
				String reason = "The user '" + sellerID + "' does not own the good '" + goodID + "'.";
				ErrorResponse payload = new ErrorResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ControllerErrorConsts.NO_PERMISSION, reason);
				return new MetaResponse(403, payload);
			}

			String signature = ownerData.getSignature();
			ownerData.setSignature("");
			boolean res = isClientWilling(sellerID, signature, ownerData);
			ownerData.setSignature(signature);
			if (!res) {
				// TODO - Rollback is not needed here. //
				conn.rollback();
				String reason = "The Seller's signature is not valid.";
				ErrorResponse payload = new ErrorResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ControllerErrorConsts.BAD_SIGNATURE, reason);
				return new MetaResponse(401, payload);
			}

			String writeOperationSignature = ownerData.getWriteOperationSignature();
			String writerID = ownerData.getOwner();
			long wts = ownerData.getWriteTimestamp();
			res = verifyWriteOnGoodsOperationSignature(goodID, ownerData.isOnSale(), writerID, wts, writeOperationSignature);
			if (!res) {
				// TODO - Rollback is not needed here. //
				conn.rollback();
				String reason = "The Write On Goods Operation's signature is not valid.";
				ErrorResponse payload = new ErrorResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ControllerErrorConsts.BAD_SIGNATURE, reason);
				return new MetaResponse(401, payload);
			}

			MarkForSale.markForSale(conn, goodID, writerID, ""+wts, writeOperationSignature);
			conn.commit();
			BasicMessage payload = new WriteResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ownerData.getWriteTimestamp());
			return new MetaResponse(payload);
		}
		// TODO - This is not necessary, execute is in a try catch that handles exceptions. //
		catch (SQLException | DBNoResultsException ex) {
			if (conn != null) {
				conn.rollback();
			}
			throw ex;
		}
		catch (SignatureException ex) {
			if (conn != null) {
				conn.rollback();
			}
			return GeneralControllerHelper.handleException(ex, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		}
		finally {
			if (conn != null) {
				conn.setAutoCommit(true);
				conn.close();
			}
		}
	}
}