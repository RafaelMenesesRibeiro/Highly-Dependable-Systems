package com.hds.server.controllers;

import com.hds.domain.SignedTransactionData;
import com.hds.domain.TransactionData;
import com.hds.exception.*;
import com.hds.helpers.ControllerErrorConsts;
import com.hds.helpers.DatabaseManager;
import com.hds.helpers.SecurityManager;
import com.hds.helpers.TransactiomValidityChecker;
import com.hds.helpers.TransferGood;
import com.hds.server.msgtypes.BasicResponse;
import com.hds.server.msgtypes.ErrorResponse;
import com.hds.server.msgtypes.SecureResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@RestController
public class TransferGoodController {
	private static final String OPERATION = "transferGood";

	@PostMapping(value = "/transferGood")
	public SecureResponse transferGood(@RequestBody SignedTransactionData signedData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Transfer Good request.");

		BasicResponse payload;
		try {
			payload = execute(signedData);
		}
		catch (IOException e) {
			payload = new ErrorResponse(403, ControllerErrorConsts.CANCER, OPERATION, e.getMessage());
		}
		catch (URISyntaxException urisex) {
			payload = new ErrorResponse(400, ControllerErrorConsts.BAD_URI, OPERATION, urisex.getMessage());
		}
		catch (InvalidQueryParameterException iqpex) {
			payload = new ErrorResponse(400, ControllerErrorConsts.BAD_PARAMS, OPERATION, iqpex.getMessage());
		}
		catch (DBConnectionRefusedException dbcrex) {
			payload = new ErrorResponse(401, ControllerErrorConsts.CONN_REF, OPERATION, dbcrex.getMessage());
		}
		catch (DBClosedConnectionException dbccex) {
			payload = new ErrorResponse(503, ControllerErrorConsts.CONN_CLOSED, OPERATION, dbccex.getMessage());
		}
		catch (DBNoResultsException dbnrex) {
			payload = new ErrorResponse(500, ControllerErrorConsts.NO_RESP, OPERATION, dbnrex.getMessage());
		}
		catch (DBSQLException | SQLException sqlex) {
			payload = new ErrorResponse(500, ControllerErrorConsts.BAD_SQL, OPERATION, sqlex.getMessage());
		}
		try {
			return new SecureResponse(payload);
		}
		catch (SignatureException se) {
			payload = new ErrorResponse(500, ControllerErrorConsts.CANCER, OPERATION, se.getMessage());
			return new SecureResponse(payload, true);
		}
	}

	private BasicResponse execute(SignedTransactionData signedData)
			throws URISyntaxException, SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException,
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
		try (Connection conn = DatabaseManager.getJDBCConnection()) {
			if (TransactiomValidityChecker.isTransactoionValid(conn, signedData)) {
				TransferGood.TransferGood(conn, sellerID, buyerID, goodID);
				return new BasicResponse(200, "ok", OPERATION);
			}
			else {
				return new ErrorResponse(403, ControllerErrorConsts.BAD_TRANSACTION, OPERATION, "NOT IMPLEMENTED YET.");
			}
		}
		catch (IncorrectSignatureException is){
			return new ErrorResponse(403, ControllerErrorConsts.BAD_TRANSACTION, OPERATION, is.getMessage());
		}
	}
}
