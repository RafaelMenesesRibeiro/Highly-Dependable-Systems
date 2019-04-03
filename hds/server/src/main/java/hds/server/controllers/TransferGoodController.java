package hds.server.controllers;

import hds.security.domain.SignedTransactionData;
import hds.security.domain.TransactionData;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.ControllerErrorConsts;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.helpers.TransferGood;
import hds.server.msgtypes.BasicResponse;
import hds.server.msgtypes.ErrorResponse;
import hds.server.msgtypes.SecureResponse;
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

	@PostMapping(value = "/transferGood")
	public ResponseEntity<SecureResponse> transferGood(@RequestBody SignedTransactionData signedData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Transfer Good request.");

		MetaResponse metaResponse;
		try {
			metaResponse = new MetaResponse(execute(signedData));
		}
		catch (IOException e) {
			metaResponse = new MetaResponse(403, new ErrorResponse(403, ControllerErrorConsts.CANCER, OPERATION, e.getMessage()));
		}
		catch (InvalidQueryParameterException iqpex) {
			metaResponse = new MetaResponse(400, new ErrorResponse(400, ControllerErrorConsts.BAD_PARAMS, OPERATION, iqpex.getMessage()));
		}
		catch (DBConnectionRefusedException dbcrex) {
			metaResponse = new MetaResponse(401, new ErrorResponse(401, ControllerErrorConsts.CONN_REF, OPERATION, dbcrex.getMessage()));
		}
		catch (DBClosedConnectionException dbccex) {
			metaResponse = new MetaResponse(503, new ErrorResponse(503, ControllerErrorConsts.CONN_CLOSED, OPERATION, dbccex.getMessage()));
		}
		catch (DBNoResultsException dbnrex) {
			metaResponse = new MetaResponse(500, new ErrorResponse(500, ControllerErrorConsts.NO_RESP, OPERATION, dbnrex.getMessage()));
		}
		catch (DBSQLException | SQLException sqlex) {
			metaResponse = new MetaResponse(500, new ErrorResponse(500, ControllerErrorConsts.BAD_SQL, OPERATION, sqlex.getMessage()));
		}
		return sendResponse(metaResponse, false);
	}

	private BasicResponse execute(SignedTransactionData signedData)
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
		try (Connection conn = DatabaseManager.getConnection()) {
			if (TransactionValidityChecker.isValidTransaction(conn, signedData)) {
				TransferGood.transferGood(conn, sellerID, buyerID, goodID);
				return new BasicResponse(200, "ok", OPERATION);
			}
			else {
				return new ErrorResponse(403, ControllerErrorConsts.BAD_TRANSACTION, OPERATION, "The transaction is not valid.");
			}
		}
		catch (IncorrectSignatureException is){
			return new ErrorResponse(403, ControllerErrorConsts.BAD_TRANSACTION, OPERATION, is.getMessage());
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
			payload = new ErrorResponse(500, ControllerErrorConsts.CANCER, OPERATION, ex.getMessage());
			return new ResponseEntity<>(new SecureResponse(payload, true), HttpStatus.valueOf(metaResponse.getStatusCode()));
		}
	}
}
