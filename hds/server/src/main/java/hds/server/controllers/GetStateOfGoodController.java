package hds.server.controllers;

import hds.server.exception.*;
import hds.server.helpers.ControllerErrorConsts;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.msgtypes.BasicResponse;
import hds.server.msgtypes.ErrorResponse;
import hds.server.msgtypes.SecureResponse;
import hds.server.msgtypes.StateOfGood;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class GetStateOfGoodController {
	private static final String OPERATION = "getStateOfGood";

	@GetMapping(value = "/stateOfGood", params = { "goodID" })
	public ResponseEntity<SecureResponse> getStateOfGood(@RequestParam("goodID") String goodID) {
		BasicResponse payload;
		try {
			payload = execute(goodID);
			return sendResponse(payload, true);
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

	private StateOfGood execute(String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException, DBNoResultsException {
		try (Connection conn = DatabaseManager.getConnection()) {
			boolean state = TransactionValidityChecker.getIsOnSale(conn, goodID);
			String ownerID = TransactionValidityChecker.getCurrentOwner(conn, goodID);
			return new StateOfGood(200, "ok", OPERATION, ownerID, state);
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

