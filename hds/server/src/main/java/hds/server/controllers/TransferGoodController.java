package hds.server.controllers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SaleCertificateResponse;
import hds.server.ServerApplication;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;
import hds.server.exception.IncorrectSignatureException;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.helpers.TransferGood;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static hds.security.DateUtils.*;
import static hds.security.SecurityManager.verifyWriteOnGoodsOperationSignature;
import static hds.security.SecurityManager.verifyWriteOnOwnershipSignature;
import static hds.server.helpers.TransactionValidityChecker.getOnOwnershipTimestamp;

/**
 * Responsible for handling POST requests for the endpoint /transferGood.
 * Confirms authenticity and integrity of the request and wrapped request.
 * Confirms the SellerID owns the GoodID.
 * Confirms the GoodID is on sale.
 * Transfers a GoodID from the SellerID to the BuyerID.
 *
 * @author 		Rafael Ribeiro
 * @see 		ApproveSaleRequestMessage
 */
@SuppressWarnings("Duplicates")
@RestController
public class TransferGoodController {
	private static final String FROM_SERVER = ServerApplication.getPort();
	private static final String OPERATION = "transferGood";
	private static final String CERTIFIED = "Certified by Notary";

	/**
	 * REST Controller responsible for transferring a GoodID.
	 *
	 * @param 	transactionData	GoodID, BuyerID and SellerID
	 * @param 	result    		result of validators for inputs of transactionData
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see		ApproveSaleRequestMessage
	 * @see 	BindingResult
	 */
	@PostMapping(value = "/transferGood",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> transferGood(@RequestBody @Valid ApproveSaleRequestMessage transactionData, BindingResult result) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Transfer Good request.");
		logger.info("\tRequest: " + transactionData.toString());

		UserRequestIDKey key = new UserRequestIDKey(transactionData.getFrom(), transactionData.getRequestID());
		ResponseEntity<BasicMessage> cachedResponse = GeneralControllerHelper.tryGetRecentRequest(key);
		if (cachedResponse != null) {
			return cachedResponse;
		}

		MetaResponse metaResponse;

		// TODO - Add this to custom validation. //
		long timestamp = transactionData.getTimestamp();
		if (!isFreshTimestamp(timestamp)) {
			String reason = "Timestamp " + timestamp + " is too old";
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getTo(), "", ControllerErrorConsts.OLD_MESSAGE, reason);
			metaResponse = new MetaResponse(408, payload);
			return GeneralControllerHelper.getResponseEntity(metaResponse, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
		}

		if(result.hasErrors()) {
			metaResponse = GeneralControllerHelper.handleInputValidationResults(result, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
			ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
			GeneralControllerHelper.cacheRecentRequest(key, response);
			return response;
		}

		try {
			metaResponse = execute(transactionData);
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
		}
		ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
		GeneralControllerHelper.cacheRecentRequest(key, response);
		return response;
	}

	/**
	 * Confirms authenticity and integrity of the request and wrapped request.
	 * Confirms the SellerID owns the GoodID.
	 * Confirms the GoodID is on sale.
	 * Transfers a GoodID from the SellerID to the BuyerID.
	 *
	 * @param 	transactionData 	GoodID and SellerID
	 * @return 	MetaResponse 		Contains an HttpStatus code and a BasicMessage
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @see 	ApproveSaleRequestMessage
	 * @see 	SaleCertificateResponse
	 * @see 	MetaResponse
	 */
	private MetaResponse execute(ApproveSaleRequestMessage transactionData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException, JSONException {

		// TODO - Remove these. //
		String buyerID = InputValidation.cleanString(transactionData.getBuyerID());
		String sellerID = InputValidation.cleanString(transactionData.getSellerID());
		String goodID = InputValidation.cleanString(transactionData.getGoodID());

		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);

			// TODO - ?? Verify Write Timestamp agains Goods Table ?? //
			long requestWriteTimestamp = transactionData.getWriteTimestamp();
			long databaseWriteTimestamp = getOnOwnershipTimestamp(conn, goodID);
			if (!isNewTimestampMoreRecent(databaseWriteTimestamp, requestWriteTimestamp)) {
				String reason = "write timestamp " + requestWriteTimestamp + " is too old";
				ErrorResponse payload = new ErrorResponse(generateTimestamp(), transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.OLD_MESSAGE, reason);
				return new MetaResponse(408, payload);
			}

			if (TransactionValidityChecker.isValidTransaction(conn, transactionData)) {
				String writeOnOwnershipsSignature = transactionData.getwriteOnOwnershipsSignature();
				int writeTimestamp = transactionData.getWts();
				boolean res = verifyWriteOnOwnershipSignature(goodID, buyerID, writeTimestamp, writeOnOwnershipsSignature);
				if (!res) {
					// TODO - Rollback is not needed here. //
					conn.rollback();
					String reason = "The Write On Ownership Operation's signature is not valid.";
					ErrorResponse payload = new ErrorResponse(generateTimestamp(), transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_SIGNATURE, reason);
					return new MetaResponse(401, payload);
				}

				String writeOnGoodsSignature = transactionData.getWriteOnGoodsSignature();
				res = verifyWriteOnGoodsOperationSignature(goodID, transactionData.getOnSale(), buyerID, writeTimestamp, writeOnGoodsSignature);
				if (!res) {
					// TODO - Rollback is not needed here. //
					conn.rollback();
					String reason = "The Write On Goods Operation's signature is not valid.";
					ErrorResponse payload = new ErrorResponse(generateTimestamp(), transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_SIGNATURE, reason);
					return new MetaResponse(401, payload);
				}

				TransferGood.transferGood(conn, goodID, buyerID, ""+writeTimestamp, writeOnOwnershipsSignature, writeOnGoodsSignature);

				conn.commit();
				SaleCertificateResponse payload = new SaleCertificateResponse(generateTimestamp(), transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", CERTIFIED, goodID, sellerID, buyerID);
				return new MetaResponse(payload);
			}
			else {
				conn.rollback();
				String reason = "The transaction is not valid.";
				ErrorResponse payload = new ErrorResponse(generateTimestamp(), transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_TRANSACTION, reason);
				return new MetaResponse(403, payload);
			}
		}
		// TODO - This is handled in the main controller method. // Just rollback and throw any. //
		catch (SQLException | DBNoResultsException ex) {
			if (conn != null) {
				conn.rollback();
			}
			throw ex;
		}
		catch (IncorrectSignatureException isex){
			if (conn != null) {
				conn.rollback();
			}
			return GeneralControllerHelper.handleException(isex, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
		}
		finally {
			if (conn != null) {
				conn.setAutoCommit(true);
				conn.close();
			}
		}
	}
}
