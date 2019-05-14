package hds.server.controllers;

import hds.security.helpers.inputValidation.inputValidation;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.GoodStateResponse;
import hds.server.ServerApplication;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;
import hds.server.helpers.DatabaseManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hds.security.DateUtils.generateTimestamp;
import static hds.server.controllers.BaseController.FROM_SERVER;
import static hds.server.helpers.TransactionValidityChecker.getOnGoodsInfo;
import static hds.server.helpers.TransactionValidityChecker.getOnOwnershipInfo;

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
	private static final String OPERATION = "getStateOfGood";

	/**
	 * REST Controller responsible for returning the state of a given GoodID.
	 * Returns information regarding the current owner of the good and whether or not it is on sale.
	 *
	 * @param 	goodID 			The GoodID which is going to be looked up (for ownerID and if it is on sale)
	 * @param 	readID 			The ReadID associated with this read operation
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 */
	@GetMapping(value = "/stateOfGood", params = { "goodID", "readID" })
	public ResponseEntity<BasicMessage> getStateOfGood(
														@RequestParam("goodID") @NotNull @NotEmpty String goodID,
														@RequestParam("readID") @NotNull @NotEmpty String readID) {

		String msg = "\nReceived request for " + OPERATION + ":\n\tGoodID - " + goodID + "\n\tReadID - " + readID;
		ServerApplication.getLogManager().log(msg);

		goodID = InputValidation.cleanString(goodID);
		MetaResponse metaResponse;
		try {
			if (!isValidGoodID(goodID)) {
				throw new IllegalArgumentException("The GoodID " + goodID + " is not valid.");
			}
			int rid = Integer.parseInt(readID);
			metaResponse = new MetaResponse(execute(goodID, rid));
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
	 * @throws 	JSONException					Can't create / parse JSONObject
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public static GoodStateResponse execute(String goodID, int readID)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			JSONObject ownershipInfo = getOnOwnershipInfo(connection, goodID);
			String ownerID = ownershipInfo.getString("userID");
			int onOwnershipWriteTimestamp = Integer.parseInt(ownershipInfo.getString("ts"));
			String writeOnOwnershipSignature = ownershipInfo.getString("sig");

			JSONObject goodsInfo = getOnGoodsInfo(connection, goodID);
			boolean state = goodsInfo.getString("onSale").equals("t");
			String onGoodsWriterID = goodsInfo.getString("wid");
			int onGoodsWriteTimestamp = Integer.parseInt(goodsInfo.getString("ts"));
			String writeOnGoodsOperationSignature = goodsInfo.getString("sig");

			return new GoodStateResponse(
					generateTimestamp(),
					NO_REQUEST_ID,
					OPERATION,
					FROM_SERVER,
					TO_UNKNOWN,
					"",
					goodID,
					ownerID,
					state,
					onGoodsWriterID,
					onGoodsWriteTimestamp,
					writeOnGoodsOperationSignature,
					onOwnershipWriteTimestamp,
					writeOnOwnershipSignature,
					readID);
		}
		catch (Exception ex) {
			if (connection != null) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw ex; // Handled in getStateOfGood's main method, in the try catch were execute is called.
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
