package hds.server.controllers;

import hds.security.helpers.inputValidation.ValidClientIDValidator;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ReadWtsResponse;
import hds.server.ServerApplication;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static hds.security.DateUtils.generateTimestamp;
import static hds.server.controllers.BaseController.FROM_SERVER;

/**
 * Responsible for handling GET requests for the endpoint /getCurrentTimestamp.
 * Returns the replica's current write timestamp.
 *
 * @author 		Rafael Ribeiro
 */
@RestController
public class GetCurrentTimestampController {
	private static final String NO_REQUEST_ID = "0";
	private static final String TO_UNKNOWN = "unknown";
	private static final String OPERATION = "getCurrentTimestamp";

	/**
	 * REST Controller responsible for returning the replica's current write timestamp.
	 *
	 * @param 	clientID 		The Client who is asking
	 * @param 	readID 			The ReadID associated with this read operation
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 */
	@GetMapping(value = "/getCurrentTimestamp", params = { "clientID", "readID" })
	public ResponseEntity<BasicMessage> getStateOfGood(
			@RequestParam("clientID") @NotNull @NotEmpty String clientID,
			@RequestParam("readID") @NotNull @NotEmpty String readID) {

		String msg = "\nReceived request for " + OPERATION + ":\n\tClientID - " + clientID + "\n\tReadID - " + readID;
		ServerApplication.getLogManager().log(msg);

		clientID = InputValidation.cleanString(clientID);
		MetaResponse metaResponse;
		try {
			if (!ValidClientIDValidator.isValid(clientID)) {
				throw new IllegalArgumentException("The ClientID " + clientID + " is not valid.");
			}
			int rid = Integer.parseInt(readID);
			metaResponse = new MetaResponse(execute(clientID, rid));
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, NO_REQUEST_ID, TO_UNKNOWN, OPERATION);
		}
		return GeneralControllerHelper.getResponseEntity(metaResponse, NO_REQUEST_ID, TO_UNKNOWN, OPERATION);
	}

	/**
	 * Gets the state of the given GoodID.
	 *
	 * @param 	clientID 			The ClientID who is asking the current timestamp
	 * @return 	ReadWtsResponse 	Contains the current timestamp and the readID
	 */
	public static ReadWtsResponse execute(String clientID, int readID) {
		long currentWriteTimestamp = ServerApplication.getCurrentWriteTimestamp();
		ReadWtsResponse response = new ReadWtsResponse(generateTimestamp(), NO_REQUEST_ID, OPERATION, FROM_SERVER, TO_UNKNOWN, "", readID, currentWriteTimestamp);
		return response;
	}
}
