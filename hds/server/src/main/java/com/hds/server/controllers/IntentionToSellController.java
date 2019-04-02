package com.hds.server.controllers;

import com.hds.domain.OwnerData;
import com.hds.domain.SignedOwnerData;
import com.hds.domain.SignedTransactionData;
import com.hds.exception.*;
import com.hds.helpers.ControllerErrorConsts;
import com.hds.helpers.DatabaseManager;
import com.hds.helpers.MarkForSale;
import com.hds.helpers.SecurityManager;
import com.hds.helpers.TransactiomValidityChecker;
import com.hds.server.msgtypes.BasicResponse;
import com.hds.server.msgtypes.ErrorResponse;
import com.hds.server.msgtypes.SecureResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@RestController
public class IntentionToSellController {
	private static final String OPERATION = "markForSale";

	@PostMapping(value = "/intentionToSell")
	public SecureResponse intentionToSell(@RequestBody SignedOwnerData signedData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Intention to Sell request.");

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

	private BasicResponse execute(SignedOwnerData signedData)
			throws URISyntaxException, SQLException, DBClosedConnectionException, DBConnectionRefusedException,
					DBSQLException, InvalidQueryParameterException, DBNoResultsException, IOException {

		OwnerData ownerData = signedData.getPayload();
		String sellerID = ownerData.getSellerID();
		String goodID = ownerData.getGoodID();

		if (sellerID == null || sellerID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'sellerID' in query 'markForSale' is either null or an empty string.");
		}
		try (Connection conn = DatabaseManager.getJDBCConnection()) {
			String ownerID = TransactiomValidityChecker.getCurrentOwner(conn, goodID);
			if (!ownerID.equals(sellerID)) {
				return new ErrorResponse(403, "You do not have permission to put this item on sale.", OPERATION, "The user '" + sellerID + "' does not own the good '" + goodID + "'.");
			}
			boolean res = TransactiomValidityChecker.isClientWilling(sellerID, signedData.getSignature(), SecurityManager.getByteArray(ownerData));
			if (!res) {
				return new ErrorResponse(403, ControllerErrorConsts.BAD_TRANSACTION, OPERATION, "The Seller's signature is not valid.");
			}
			MarkForSale.markForSale(conn, goodID);
			return new BasicResponse(200, "ok", OPERATION);
		}
		catch (SignatureException is){
			return new ErrorResponse(403, ControllerErrorConsts.BAD_TRANSACTION, OPERATION, is.getMessage());
		}
	}
}