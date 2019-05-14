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
@RestController
public class IntentionToSellController extends BaseController {

	private IntentionToSellController() {
		OPERATION = "intentionToSell";
	}

	/**
	 * REST Controller responsible for marking a goodID for sale.
	 *
	 * @param 	ownerData 		OwnerDataMessage
	 * @param 	result    		result of validators for inputs of ownerData
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see		OwnerDataMessage
	 * @see 	BindingResult
	 */
	@PostMapping(value = "/intentionToSell",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> intentionToSell(@RequestBody @Valid OwnerDataMessage ownerData, BindingResult result) {
		return GeneralControllerHelper.generalControllerSetup(ownerData, result, this);
	}

	/**
	 * Confirms authenticity and integrity of the request.
	 * Marks a GoodID for sale in the database.
	 *
	 * @param 	requestData			OwnerDataMessage
	 * @return 	MetaResponse 		Contains an HttpStatus code and a BasicMessage
	 * @throws 	JSONException					Can't create / parse JSONObject
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @see 	OwnerDataMessage
	 * @see 	MetaResponse
	 */
	@Override
	public MetaResponse execute(BasicMessage requestData)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException,
			DBNoResultsException, OldMessageException, NoPermissionException {

		OwnerDataMessage ownerData = (OwnerDataMessage) requestData;

		String sellerID = InputValidation.cleanString(ownerData.getOwner());
		String goodID = InputValidation.cleanString(ownerData.getGoodID());

		String signature = ownerData.getSignature();
		ownerData.setSignature("");
		boolean res = isClientWilling(sellerID, signature, ownerData);
		ownerData.setSignature(signature);
		if (!res) {
			throw new SignatureException("The Seller's signature is not valid.");
		}

		long requestWriteTimestamp = ownerData.getWriteTimestamp();
		String writeOperationSignature = ownerData.getWriteOperationSignature();
		String writerID = ownerData.getOwner();
		res = verifyWriteOnGoodsOperationSignature(goodID, ownerData.isOnSale(), writerID, requestWriteTimestamp, writeOperationSignature);
		if (!res) {
			throw new SignatureException("The Write On Goods Operation's signature is not valid.");
		}

		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			String ownerID = getCurrentOwner(connection, goodID);
			if (!ownerID.equals(sellerID)) {
				connection.rollback();
				throw new NoPermissionException("The user '" + sellerID + "' does not own the good '" + goodID + "'.");
			}

			synchronized (this) {
				long writeTimestamp = ServerApplication.getCurrentWriteTimestamp();
				res = requestWriteTimestamp > writeTimestamp;
				if (!res) {
					throw new OldMessageException("Write Timestamp " + requestWriteTimestamp + " is too old.");
				}
				MarkForSale.changeGoodSaleStatus(connection, goodID, true, writerID, ""+requestWriteTimestamp, writeOperationSignature);
			}

			connection.commit();
			BasicMessage payload = new WriteResponse(generateTimestamp(), ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ownerData.getWriteTimestamp());
			return new MetaResponse(payload);
		}
		catch (Exception ex) {
			if (connection != null) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw ex; // Handled in intentionToSell's main method, in the try catch were execute is called.
		}
	}
}