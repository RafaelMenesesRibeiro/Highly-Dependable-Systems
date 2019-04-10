package hds.server.controllers;

import hds.security.CryptoUtils;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.GoodDataMessage;
import hds.security.msgtypes.GoodStateResponse;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@RestController
public class GetStateOfGoodController {
	private static final String FROM_SERVER = "server";
	private static final String OPERATION = "getStateOfGood";

	@GetMapping(value = "/stateOfGood", params = { "goodID" })
	public ResponseEntity<BasicMessage> getStateOfGood(@RequestParam("goodID") String goodID) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Get State of Good request.");
		logger.info("\tGoodID - " + goodID);

		MetaResponse metaResponse;
		try {
			goodID = InputValidation.cleanString(goodID);
			InputValidation.isValidGoodID(goodID);
			metaResponse = new MetaResponse(execute(goodID));
			return sendResponse(metaResponse, true);
		}
		catch (IllegalArgumentException | InvalidQueryParameterException ex) {
			ErrorResponse payload = new ErrorResponse("0", OPERATION, FROM_SERVER, "unkwown", "", ControllerErrorConsts.BAD_PARAMS, ex.getMessage());
			metaResponse = new MetaResponse(400, payload);
		}
		catch (DBConnectionRefusedException dbcrex) {
			ErrorResponse payload = new ErrorResponse("0", OPERATION, FROM_SERVER, "unkwown", "", ControllerErrorConsts.CONN_REF, dbcrex.getMessage());
			metaResponse = new MetaResponse(401, payload);
		}
		catch (DBClosedConnectionException dbccex) {
			ErrorResponse payload = new ErrorResponse("0", OPERATION, FROM_SERVER, "unkwown", "", ControllerErrorConsts.CONN_CLOSED, dbccex.getMessage());
			metaResponse = new MetaResponse(503, payload);
		}
		catch (DBNoResultsException dbnrex) {
			ErrorResponse payload = new ErrorResponse("0", OPERATION, FROM_SERVER, "unkwown", "", ControllerErrorConsts.NO_RESP, dbnrex.getMessage());
			metaResponse = new MetaResponse(500, payload);
		}
		catch (DBSQLException | SQLException sqlex) {
			ErrorResponse payload = new ErrorResponse("0", OPERATION, FROM_SERVER, "unkwown", "", ControllerErrorConsts.BAD_SQL, sqlex.getMessage());
			metaResponse = new MetaResponse(500, payload);
		}
		return sendResponse(metaResponse, false);
	}

	private GoodStateResponse execute(String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException, DBNoResultsException {
		try (Connection conn = DatabaseManager.getConnection()) {
			boolean state = TransactionValidityChecker.getIsOnSale(conn, goodID);
			String ownerID = TransactionValidityChecker.getCurrentOwner(conn, goodID);
			return new GoodStateResponse("0", OPERATION, FROM_SERVER, "unkwown", "", ownerID, state);
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

