package hds.server.controllers;

import hds.security.helpers.inputValidation.ValidGoodID;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.GoodStateResponse;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;
import hds.server.exception.DBSQLException;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@RestController
public class GetStateOfGoodController {
	private static final String TO_UNKNOWN = "unknown";
	private static final String FROM_SERVER = "server";
	private static final String OPERATION = "getStateOfGood";

	@SuppressWarnings("Duplicates")
	@GetMapping(value = "/stateOfGood", params = { "goodID" })
	public ResponseEntity<BasicMessage> getStateOfGood(@RequestParam("goodID") @NotNull @NotEmpty @ValidGoodID String goodID, BindingResult result) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Get State of Good request.");
		logger.info("\tGoodID - " + goodID);

		MetaResponse metaResponse;
		if(result.hasErrors()) {
			metaResponse = GeneralControllerHelper.handleInputValidationResults(result, "0", TO_UNKNOWN, OPERATION);
			return GeneralControllerHelper.getResponseEntity(metaResponse, "0", TO_UNKNOWN, OPERATION);
		}

		try {
			goodID = InputValidation.cleanString(goodID);
			metaResponse = new MetaResponse(execute(goodID));
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, "0", TO_UNKNOWN, OPERATION);
		}
		return GeneralControllerHelper.getResponseEntity(metaResponse, "0", TO_UNKNOWN, OPERATION);
	}

	private GoodStateResponse execute(String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, DBNoResultsException {
		try (Connection conn = DatabaseManager.getConnection()) {
			boolean state = TransactionValidityChecker.getIsOnSale(conn, goodID);
			String ownerID = TransactionValidityChecker.getCurrentOwner(conn, goodID);
			return new GoodStateResponse("0", OPERATION, FROM_SERVER, TO_UNKNOWN, "", ownerID, state);
		}
	}
}

