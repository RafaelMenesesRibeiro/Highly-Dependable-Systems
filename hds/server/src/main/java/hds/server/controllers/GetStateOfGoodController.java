package hds.server.controllers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.responses.BasicResponse;
import hds.security.msgtypes.responses.ErrorResponse;
import hds.security.msgtypes.responses.GoodState;
import hds.security.msgtypes.responses.SecureResponse;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.controllers.security.InputValidation;
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
	private static final String OPERATION = "getStateOfGood";

	@GetMapping(value = "/stateOfGood", params = { "goodID" })
	public ResponseEntity<SecureResponse> getStateOfGood(@RequestParam("goodID") String goodID) {
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
			metaResponse = new MetaResponse(400, new ErrorResponse(ControllerErrorConsts.BAD_PARAMS, OPERATION, ex.getMessage()));
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

	private GoodState execute(String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException, DBNoResultsException {
		try (Connection conn = DatabaseManager.getConnection()) {
			boolean state = TransactionValidityChecker.getIsOnSale(conn, goodID);
			String ownerID = TransactionValidityChecker.getCurrentOwner(conn, goodID);
			return new GoodState("ok", OPERATION, ownerID, state);
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

