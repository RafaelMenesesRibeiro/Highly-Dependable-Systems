package hds.server.controllers;

import hds.security.SecurityManager;
import hds.security.domain.SignedTransactionData;
import hds.security.domain.TransactionData;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.responses.BasicResponse;
import hds.security.msgtypes.responses.ErrorResponse;
import hds.security.msgtypes.responses.SecureResponse;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.InputProcessor;
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
	private static final String OPERATION = "transferGood";

	@PostMapping(value = "/transferGood",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<SecureResponse> transferGood(@RequestBody SignedTransactionData signedData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Transfer Good request.");

		TransactionData transactionData = signedData.getPayload();
		String buyerID = transactionData.getBuyerID();
		String sellerID = transactionData.getSellerID();
		String goodID = transactionData.getGoodID();
		logger.info("\tBuyerID - " + buyerID);
		logger.info("\tSellerID - " + sellerID);
		logger.info("\tGoodID - " + goodID);
		MetaResponse metaResponse;
		try {
			InputProcessor.isValidString(sellerID);
			InputProcessor.isValidString(buyerID);
			InputProcessor.isValidString(goodID);
			metaResponse = execute(signedData);
		}
		catch (InvalidStringException | InvalidQueryParameterException ex) {
			metaResponse = new MetaResponse(400, new ErrorResponse(ControllerErrorConsts.BAD_PARAMS, OPERATION, ex.getMessage()));
		}
		catch (IOException e) {
			metaResponse = new MetaResponse(403, new ErrorResponse(ControllerErrorConsts.CANCER, OPERATION, e.getMessage()));
		}
		catch (DBConnectionRefusedException dbcrex) {
			metaResponse = new MetaResponse(401, new ErrorResponse(ControllerErrorConsts.CONN_REF, OPERATION, dbcrex.getMessage()));
		}
		catch (DBClosedConnectionException dbccex) {
			metaResponse = new MetaResponse(503, new ErrorResponse(ControllerErrorConsts.CONN_CLOSED, OPERATION, dbccex.getMessage()));
		}
		catch (DBNoResultsException dbnrex) {
			metaResponse = new MetaResponse(500, new ErrorResponse(ControllerErrorConsts.NO_RESP, OPERATION, dbnrex.getMessage()));
		}
		catch (DBSQLException | SQLException sqlex) {
			metaResponse = new MetaResponse(500, new ErrorResponse(ControllerErrorConsts.BAD_SQL, OPERATION, sqlex.getMessage()));
		}
		return sendResponse(metaResponse, false);
	}

	private MetaResponse execute(SignedTransactionData signedData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException,
					InvalidQueryParameterException, DBNoResultsException, IOException {

		TransactionData transactionData = signedData.getPayload();
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

		if (buyerID.equals(SecurityManager.SELLER_INCORRECT_BUYER_SIGNATURE)) {
			return new MetaResponse(403, new ErrorResponse(ControllerErrorConsts.BAD_SIGNATURE, OPERATION, "The seller thinks your signature is incorrect."));
		}

		try (Connection conn = DatabaseManager.getConnection()) {
			if (TransactionValidityChecker.isValidTransaction(conn, signedData)) {
				TransferGood.transferGood(conn, sellerID, buyerID, goodID);
				return new MetaResponse(new BasicResponse("ok", OPERATION));
			}
			else {
				return new MetaResponse(403, new ErrorResponse(ControllerErrorConsts.BAD_TRANSACTION, OPERATION, "The transaction is not valid."));
			}
		}
		catch (IncorrectSignatureException is){
			return new MetaResponse(403, new ErrorResponse(ControllerErrorConsts.BAD_TRANSACTION, OPERATION, is.getMessage()));
		}
	}

	@SuppressWarnings("Duplicates")
	private ResponseEntity<SecureResponse> sendResponse(MetaResponse metaResponse, boolean isSuccess) {
		BasicResponse payload = metaResponse.getPayload();
		try {
			if (isSuccess) {
				return new ResponseEntity<>(new SecureResponse(payload), HttpStatus.OK);
			}
			return new ResponseEntity<>(new SecureResponse(payload), HttpStatus.valueOf(metaResponse.getStatusCode()));
		}
		catch (SignatureException ex) {
			payload = new ErrorResponse(ControllerErrorConsts.CANCER, OPERATION, ex.getMessage());
			return new ResponseEntity<>(new SecureResponse(payload, ""), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
