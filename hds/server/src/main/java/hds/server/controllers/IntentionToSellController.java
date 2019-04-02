package hds.server.controllers;

import hds.security.domain.OwnerData;
import hds.security.domain.SignedOwnerData;
import hds.server.exception.*;
import hds.server.helpers.ControllerErrorConsts;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.MarkForSale;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.msgtypes.BasicResponse;
import hds.server.msgtypes.ErrorResponse;
import hds.server.msgtypes.SecureResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static hds.security.SecurityManager.getByteArray;

@SuppressWarnings("Duplicates")
@RestController
public class IntentionToSellController {
	private static final String OPERATION = "markForSale";

	@PostMapping(value = "/intentionToSell")
	public ResponseEntity<SecureResponse> intentionToSell(@RequestBody SignedOwnerData signedData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Intention to Sell request.");

		BasicResponse payload;
		try {
			payload = execute(signedData);
		}
		catch (IOException e) {
			payload = new ErrorResponse(403, ControllerErrorConsts.CANCER, OPERATION, e.getMessage());
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
		return sendResponse(payload, false);
	}

	private BasicResponse execute(SignedOwnerData signedData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException,
					DBSQLException, InvalidQueryParameterException, DBNoResultsException, IOException {

		OwnerData ownerData = signedData.getPayload();
		String sellerID = ownerData.getSellerID();
		String goodID = ownerData.getGoodID();

		if (sellerID == null || sellerID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'sellerID' in query 'markForSale' is either null or an empty string.");
		}
		try (Connection conn = DatabaseManager.getConnection()) {
			String ownerID = TransactionValidityChecker.getCurrentOwner(conn, goodID);
			if (!ownerID.equals(sellerID)) {
				return new ErrorResponse(403, "You do not have permission to put this item on sale.", OPERATION, "The user '" + sellerID + "' does not own the good '" + goodID + "'.");
			}
			boolean res = TransactionValidityChecker.isClientWilling(sellerID, signedData.getSignature(), getByteArray(ownerData));
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

	@SuppressWarnings("Duplicates")
	private ResponseEntity<SecureResponse> sendResponse(BasicResponse payload, boolean isSuccess) {
		try {
			if (isSuccess) {
				return new ResponseEntity<>(new SecureResponse(payload), HttpStatus.OK);
			}
			return new ResponseEntity<>(new SecureResponse(payload), HttpStatus.valueOf(payload.getCode()));
		}
		catch (SignatureException ex) {
			payload = new ErrorResponse(500, ControllerErrorConsts.CANCER, OPERATION, ex.getMessage());
			return new ResponseEntity<>(new SecureResponse(payload, true), HttpStatus.valueOf(payload.getCode()));
		}
	}
}