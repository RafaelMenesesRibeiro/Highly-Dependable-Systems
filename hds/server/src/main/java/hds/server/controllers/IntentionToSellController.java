package hds.server.controllers;

import hds.security.exceptions.SignatureException;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.OwnerDataMessage;
import hds.security.msgtypes.WriteResponse;
import hds.server.ServerApplication;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.MarkForSale;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.acl.NotOwnerException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static hds.security.DateUtils.*;
import static hds.security.SecurityManager.verifyWriteOnGoodsOperationSignature;
import static hds.server.helpers.TransactionValidityChecker.*;

/**
 * Responsible for handling POST requests for the endpoint /intentionToSell.
 * Confirms authenticity and integrity of the request.
 * Marks a GoodID for sale in the database.
 *
 * @author 		Rafael Ribeiro
 * @see 		OwnerDataMessage
 */
@SuppressWarnings("Duplicates")
@RestController
public class IntentionToSellController {
	private static final String FROM_SERVER = ServerApplication.getPort();
	private static final String OPERATION = "markForSale";

	/**
	 * REST Controller responsible for marking a goodID for sale.
	 *
	 * @param 	ownerData 		GoodID and SellerID
	 * @param 	result    		result of validators for inputs of ownerData
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see		OwnerDataMessage
	 * @see 	BindingResult
	 */
	@PostMapping(value = "/intentionToSell",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> intentionToSell(@RequestBody @Valid OwnerDataMessage ownerData, BindingResult result) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Intention to Sell request.");
		logger.info("\tRequest: " + ownerData.toString());

		UserRequestIDKey key = new UserRequestIDKey(ownerData.getFrom(), ownerData.getRequestID());
		ResponseEntity<BasicMessage> cachedResponse = GeneralControllerHelper.tryGetRecentRequest(key);
		if (cachedResponse != null) {
			return cachedResponse;
		}

		MetaResponse metaResponse;

		if(result.hasErrors()) {
			metaResponse = GeneralControllerHelper.handleInputValidationResults(result, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
			ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
			GeneralControllerHelper.cacheRecentRequest(key, response);
			return response;
		}

		try {
			metaResponse = execute(ownerData);
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		}
		ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		GeneralControllerHelper.cacheRecentRequest(key, response);
		return response;
	}

	/**
	 * Confirms authenticity and integrity of the request.
	 * Marks a GoodID for sale in the database.
	 *
	 * @param 	ownerData 			GoodID and SellerID
	 * @return 	MetaResponse 		Contains an HttpStatus code and a BasicMessage
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @see 	OwnerDataMessage
	 * @see 	MetaResponse
	 */
	private MetaResponse execute(OwnerDataMessage ownerData)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException,
					DBNoResultsException, OldMessageException, NoPermissionException {

		String sellerID = InputValidation.cleanString(ownerData.getOwner());
		String goodID = InputValidation.cleanString(ownerData.getGoodID());
		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			long requestWriteTimestamp = ownerData.getWriteTimestamp();
			long databaseWriteTimestamp = getOnGoodsTimestamp(connection, goodID);
			if (!isOneTimestampAfterAnother(requestWriteTimestamp, databaseWriteTimestamp)) {
				connection.close();
				throw new OldMessageException("Write Timestamp " + requestWriteTimestamp + " is too old.");
			}

			String ownerID = getCurrentOwner(connection, goodID);
			if (!ownerID.equals(sellerID)) {
				connection.close();
				throw new NoPermissionException("The user '" + sellerID + "' does not own the good '" + goodID + "'.");
			}

			String signature = ownerData.getSignature();
			ownerData.setSignature("");
			boolean res = isClientWilling(sellerID, signature, ownerData);
			ownerData.setSignature(signature);
			if (!res) {
				connection.close();
				throw new SignatureException("The Seller's signature is not valid.");
			}

			String writeOperationSignature = ownerData.getWriteOperationSignature();
			String writerID = ownerData.getOwner();
			res = verifyWriteOnGoodsOperationSignature(goodID, ownerData.isOnSale(), writerID, requestWriteTimestamp, writeOperationSignature);
			if (!res) {
				connection.close();
				throw new SignatureException("The Write On Goods Operation's signature is not valid.");
			}

			MarkForSale.markForSale(connection, goodID, writerID, ""+requestWriteTimestamp, writeOperationSignature);
			connection.commit();
			connection.close();
			BasicMessage payload = new WriteResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ownerData.getWriteTimestamp());
			return new MetaResponse(payload);
		}
		catch (Exception ex) {
			if (connection != null) {
				connection.rollback();
				connection.setAutoCommit(true);
				connection.close();
			}
			throw ex; // Handled in intentionToSell's main method, in the try catch were execute is called.
		}
	}
}