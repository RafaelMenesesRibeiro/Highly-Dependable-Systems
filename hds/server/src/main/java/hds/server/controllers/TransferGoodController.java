package hds.server.controllers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SaleCertificateResponse;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.helpers.TransferGood;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.DateUtils.isFreshTimestamp;

@RestController
public class TransferGoodController {
	private static final String FROM_SERVER = "server";
	private static final String OPERATION = "transferGood";
	private static final String CERTIFIED = "Certified by Notary";

	@SuppressWarnings("Duplicates")
	@PostMapping(value = "/transferGood",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> transferGood(@RequestBody ApproveSaleRequestMessage transactionData, BindingResult result) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Transfer Good request.");
		logger.info("\tRequest: " + transactionData.toString());

		UserRequestIDKey key = new UserRequestIDKey(transactionData.getFrom(), transactionData.getRequestID());
		ResponseEntity<BasicMessage> cachedResponse = GeneralControllerHelper.tryGetRecentRequest(key);
		if (cachedResponse != null) {
			return cachedResponse;
		}

		long timestamp = transactionData.getTimestamp();
		if (!isFreshTimestamp(timestamp)) {
			String reason = "Timestamp " + timestamp + " is too old";
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getTo(), "", ControllerErrorConsts.OLD_MESSAGE, reason);
			MetaResponse metaResponse = new MetaResponse(408, payload);
			return GeneralControllerHelper.getResponseEntity(metaResponse, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
		}

		MetaResponse metaResponse;
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

	private MetaResponse execute(ApproveSaleRequestMessage transactionData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException,
					DBNoResultsException {

		String buyerID = InputValidation.cleanString(transactionData.getBuyerID());
		String sellerID = InputValidation.cleanString(transactionData.getSellerID());
		String goodID = InputValidation.cleanString(transactionData.getGoodID());

		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);
			if (TransactionValidityChecker.isValidTransaction(conn, transactionData)) {
				TransferGood.transferGood(conn, sellerID, buyerID, goodID);
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
		catch (IncorrectSignatureException isex){
			if (conn != null) {
				conn.rollback();
			}
			return GeneralControllerHelper.handleException(isex, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
		}
		finally {
			if (conn != null) {
				conn.setAutoCommit(true);
				// TODO - Should this close? //
				conn.close();
			}
		}
	}
}
