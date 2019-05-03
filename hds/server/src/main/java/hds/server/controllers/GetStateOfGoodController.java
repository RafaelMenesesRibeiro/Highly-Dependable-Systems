package hds.server.controllers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.helpers.inputValidation.inputValidation;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.GoodStateResponse;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hds.security.DateUtils.generateTimestamp;

/**
 * Responsible for handling GET requests for the endpoint /stateOfGood.
 * With a GoodID, returns the current owner and if it is on sale.
 *
 * @author 		Rafael Ribeiro
 */
@RestController
public class GetStateOfGoodController {
	private static final String NO_REQUEST_ID = "0";
	private static final String TO_UNKNOWN = "unknown";
	private static final String FROM_SERVER = "server";
	private static final String OPERATION = "getStateOfGood";

	/**
	 * REST Controller responsible for returning the state of a given GoodID.
	 * Returns information regarding the current owner of the good and whether or not it is on sale.
	 *
	 * @param 	goodID 			The GoodID which is going to be looked up (for ownerID and if it is on sale)
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 */
	@GetMapping(value = "/stateOfGood", params = { "goodID" })
	public ResponseEntity<BasicMessage> getStateOfGood(@RequestParam("goodID") @NotNull @NotEmpty String goodID) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Get State of Good request.");
		logger.info("\tGoodID - " + goodID);

		MetaResponse metaResponse;
		if (!isValidGoodID(goodID)) {
			String reason = "The GoodID " + goodID + " is not valid.";
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), NO_REQUEST_ID, OPERATION, FROM_SERVER, TO_UNKNOWN, "", ControllerErrorConsts.BAD_PARAMS, reason);
			metaResponse = new MetaResponse(400, payload);
			return GeneralControllerHelper.getResponseEntity(metaResponse, NO_REQUEST_ID, TO_UNKNOWN, OPERATION);
		}

		try {
			goodID = InputValidation.cleanString(goodID);
			metaResponse = new MetaResponse(execute(goodID));
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, NO_REQUEST_ID, TO_UNKNOWN, OPERATION);
		}
		return GeneralControllerHelper.getResponseEntity(metaResponse, NO_REQUEST_ID, TO_UNKNOWN, OPERATION);
	}


	/**
	 * Gets the state of the given GoodID.
	 *
	 * @param 	goodID 				The GoodID which is going to be looked up (for ownerID and if it is on sale)
	 * @return 	GoodStateResponse 	Contains the state of the goodID
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	private GoodStateResponse execute(String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {
		try (Connection conn = DatabaseManager.getConnection()) {
			boolean state = TransactionValidityChecker.getIsOnSale(conn, goodID);
			String ownerID = TransactionValidityChecker.getCurrentOwner(conn, goodID);
			return new GoodStateResponse(generateTimestamp(), NO_REQUEST_ID, OPERATION, FROM_SERVER, TO_UNKNOWN, "", ownerID, state);
		}
	}

	/**
	 * Returns whether the GoodID is valid or not.
	 * The GoodID String needs start with "good" followed by digits and a new line.
	 *
	 * @param	value 	The String to verify
	 * @return			Represents if the value is a valid GoodID
	 */
	private static boolean isValidGoodID(String value) {
		value = inputValidation.cleanString(value);
		Pattern pattern = Pattern.compile("^good[0-9]+$");
		Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}
}
