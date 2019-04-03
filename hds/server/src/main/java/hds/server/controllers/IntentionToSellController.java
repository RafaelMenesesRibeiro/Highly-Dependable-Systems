package hds.server.controllers;

import hds.security.domain.OwnerData;
import hds.security.msgtypes.BasicResponse;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SecureResponse;
import hds.server.domain.MetaResponse;
import hds.security.domain.SignedOwnerData;
import hds.server.exception.*;
import hds.security.helpers.ControllerErrorConsts;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.MarkForSale;
import hds.server.helpers.TransactionValidityChecker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

		MetaResponse metaResponse;
		try {
			metaResponse = execute(signedData);
		}
		catch (IOException e) {
			metaResponse = new MetaResponse(403, new ErrorResponse(ControllerErrorConsts.CANCER, OPERATION, e.getMessage()));
		}
		catch (InvalidQueryParameterException iqpex) {
			metaResponse = new MetaResponse(400, new ErrorResponse(ControllerErrorConsts.BAD_PARAMS, OPERATION, iqpex.getMessage()));
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

	private MetaResponse execute(SignedOwnerData signedData)
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
				return new MetaResponse(403, new ErrorResponse("You do not have permission to put this item on sale.", OPERATION, "The user '" + sellerID + "' does not own the good '" + goodID + "'."));
			}
			boolean res = TransactionValidityChecker.isClientWilling(sellerID, signedData.getSignature(), getByteArray(ownerData));
			if (!res) {
				return new MetaResponse(403, new ErrorResponse(ControllerErrorConsts.BAD_TRANSACTION, OPERATION, "The Seller's signature is not valid."));
			}
			MarkForSale.markForSale(conn, goodID);
			return new MetaResponse(new BasicResponse("ok", OPERATION));
		}
		catch (SignatureException is){
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
			return new ResponseEntity<>(new SecureResponse(payload, true), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}