package hds.server.controllers;

import hds.security.CryptoUtils;
import hds.security.ResourceManager;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.controllers.security.InputValidation;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.helpers.TransferGood;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@SuppressWarnings("Duplicates")
@RestController
public class TransferGoodController {
	private static final String FROM_SERVER = "server";
	private static final String OPERATION = "transferGood";

	@PostMapping(value = "/transferGood",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> transferGood(@RequestBody ApproveSaleRequestMessage transactionData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Transfer Good request.");

		String buyerID = InputValidation.cleanString(transactionData.getBuyerID());
		String sellerID = InputValidation.cleanString(transactionData.getSellerID());
		String goodID = InputValidation.cleanString(transactionData.getGoodID());
		logger.info("\tBuyerID - " + buyerID);
		logger.info("\tSellerID - " + sellerID);
		logger.info("\tGoodID - " + goodID);
		MetaResponse metaResponse;
		try {
			InputValidation.isValidClientID(sellerID);
			InputValidation.isValidClientID(buyerID);
			InputValidation.isValidGoodID(goodID);
			metaResponse = execute(transactionData);
		}
		catch (IllegalArgumentException | InvalidQueryParameterException ex) {
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_PARAMS, ex.getMessage());
			metaResponse = new MetaResponse(400, payload);
		}
		catch (IOException ioex) {
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.CANCER, ioex.getMessage());
			metaResponse = new MetaResponse(403, payload);
		}
		catch (DBConnectionRefusedException dbcrex) {
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.CONN_REF, dbcrex.getMessage());
			metaResponse = new MetaResponse(401, payload);
		}
		catch (DBClosedConnectionException dbccex) {
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.CONN_CLOSED, dbccex.getMessage());
			metaResponse = new MetaResponse(503, payload);
		}
		catch (DBNoResultsException dbnrex) {
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.NO_RESP, dbnrex.getMessage());
			metaResponse = new MetaResponse(500, payload);
		}
		catch (DBSQLException | SQLException sqlex) {
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_SQL, sqlex.getMessage());
			metaResponse = new MetaResponse(500, payload);
		}
		return sendResponse(metaResponse, false);
	}

	private MetaResponse execute(ApproveSaleRequestMessage transactionData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException,
					InvalidQueryParameterException, DBNoResultsException, IOException {

		String buyerID = transactionData.getBuyerID();
		String sellerID = transactionData.getSellerID();
		String goodID = transactionData.getGoodID();

		if (sellerID == null || sellerID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'sellerID' in query 'transferGood' is either null or an empty string.");
		}
		if (buyerID == null || buyerID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'buyerID' in query 'transferGood' is either null or an empty string.");
		}
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'transferGood' is either null or an empty string.");
		}

		try (Connection conn = DatabaseManager.getConnection()) {
			if (TransactionValidityChecker.isValidTransaction(conn, transactionData)) {
				TransferGood.transferGood(conn, sellerID, buyerID, goodID);
				BasicMessage payload = new BasicMessage(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "");
				return new MetaResponse(payload);
			}
			else {
				String reason = "The transaction is not valid.";
				ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_TRANSACTION, reason);
				return new MetaResponse(403, payload);
			}
		}
		catch (IncorrectSignatureException is){
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_TRANSACTION, is.getMessage());
			return new MetaResponse(403, payload);
		}
	}

	@SuppressWarnings("Duplicates")
	private ResponseEntity<BasicMessage> sendResponse(MetaResponse metaResponse, boolean isSuccess) {
		BasicMessage payload = metaResponse.getPayload();
		try {
			payload.setSignature(CryptoUtils.signData(payload));
			if (isSuccess) {
				return new ResponseEntity<>(payload, HttpStatus.OK);
			}
			return new ResponseEntity<>(payload, HttpStatus.valueOf(metaResponse.getStatusCode()));
		}
		catch (SignatureException ex) {
			ErrorResponse unsignedPayload = new ErrorResponse("0", OPERATION, FROM_SERVER, "unkwown", "", ControllerErrorConsts.CANCER, ex.getMessage());
			return new ResponseEntity<>(unsignedPayload, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
