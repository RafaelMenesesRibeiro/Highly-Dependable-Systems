package hds.server.controllers;

import hds.security.exceptions.SignatureException;
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
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.helpers.TransferGood;
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
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException, OldMessageException {

		String buyerID = InputValidation.cleanString(transactionData.getBuyerID());
		String sellerID = InputValidation.cleanString(transactionData.getSellerID());
		String goodID = InputValidation.cleanString(transactionData.getGoodID());

		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			// TODO - ?? Verify Write Timestamp agains Goods Table ?? //
			long requestWriteTimestamp = transactionData.getWts();
			long databaseWriteTimestamp = getOnOwnershipTimestamp(connection, goodID);
			if (!isOneTimestampAfterAnother(requestWriteTimestamp, databaseWriteTimestamp)) {
				connection.close();
				throw new SignatureException("Write Timestamp " + requestWriteTimestamp + " is too old");
			}

			if (!TransactionValidityChecker.isValidTransaction(connection, transactionData)) {
				connection.close();
				throw new SignatureException("The transaction is not valid.");
			}

			String writeOnOwnershipsSignature = transactionData.getWriteOnOwnershipsSignature();
			boolean res = verifyWriteOnOwnershipSignature(goodID, buyerID, requestWriteTimestamp, writeOnOwnershipsSignature);
			if (!res) {
				connection.close();
				throw new SignatureException("The Write On Ownership Operation's signature is not valid.");
			}

			String writeOnGoodsSignature = transactionData.getWriteOnGoodsSignature();
			res = verifyWriteOnGoodsOperationSignature(goodID, transactionData.getOnSale(), buyerID, requestWriteTimestamp, writeOnGoodsSignature);
			if (!res) {
				connection.close();
				throw new SignatureException("The Write On Goods Operation's signature is not valid.");
			}

			TransferGood.transferGood(connection, goodID, buyerID, ""+requestWriteTimestamp, writeOnOwnershipsSignature, writeOnGoodsSignature);
			connection.commit();
			connection.close();
			SaleCertificateResponse payload = new SaleCertificateResponse(
					generateTimestamp(),
					transactionData.getRequestID(),
					OPERATION,
					FROM_SERVER,
					transactionData.getFrom(),
					"",
					CERTIFIED,
					goodID,
					sellerID,
					buyerID,
					transactionData.getWts());
			return new MetaResponse(payload);
		}
		catch (Exception ex) {
			if (connection != null) {
				connection.rollback();
				connection.setAutoCommit(true);
				connection.close();
			}
			throw ex; // Handled in intentionToSell's main method, in the try catch were execute is called.
		}
	}
}
