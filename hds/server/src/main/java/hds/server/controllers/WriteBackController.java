package hds.server.controllers;

import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.*;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.FailedWriteBackException;
import hds.server.exception.OldMessageException;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.MarkForSale;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.DateUtils.isOneTimestampAfterAnother;
import static hds.security.SecurityManager.verifyWriteOnGoodsOperationSignature;
import static hds.security.SecurityManager.verifyWriteOnOwnershipSignature;
import static hds.server.helpers.MarkForSale.changeGoodSaleStatus;
import static hds.server.helpers.TransactionValidityChecker.*;
import static hds.server.helpers.TransferGood.changeGoodOwner;

/**
 * Responsible for handling POST requests for write back operation needed in (1, N) Byzantine Atomic Registers.
 *
 * @author 		Rafael Ribeiro
 */
@SuppressWarnings("Duplicates")
@RestController
public class WriteBackController extends BaseController {
	private WriteBackController() {
		OPERATION = "writeBack";
	}

	/**
	 * REST Controller responsible for the write back operation of (1, N) Byzantine Atomic Registers.
	 *
	 * @param 	writeBackData   WriteBackMessage
	 * @param 	result    		result of validators for inputs of transactionData
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see		BasicMessage
	 * @see 	BindingResult
	 */
	@PostMapping(value = "/writeBack",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> writeBack(@RequestBody @Valid WriteBackMessage writeBackData, BindingResult result) {
		return GeneralControllerHelper.generalControllerSetup(writeBackData, result, this);
	}


	/**
	 * Writes to the database's tables, if the received entries are more recent than the ones in it.
	 *
	 * @param   requestData     WriteBackMessage
	 * @return 	MetaResponse 	Contains an HttpStatus code and a BasicMessage
	 * @throws 	SQLException	The DB threw an SQLException
	 * @see 	ApproveSaleRequestMessage
	 */
	@Override
	public MetaResponse execute(BasicMessage requestData) throws SQLException, JSONException {
		WriteBackMessage writeBackMessage = (WriteBackMessage) requestData;
		String clientID = InputValidation.cleanString(requestData.getFrom());

		String signature = requestData.getSignature();
		requestData.setSignature("");
		boolean res = isClientWilling(clientID, signature, requestData);
		if (!res) {
			throw new SignatureException("The Client's signature is not valid.");
		}

		// TODO - Check Server's signature for GoodStateResponse. //

		GoodStateResponse onGoodsRelevantResponse = writeBackMessage.getHighestGoodState();
		String onGoodsGoodID  = InputValidation.cleanString(onGoodsRelevantResponse.getGoodID());
		String onGoodsOwnerID = InputValidation.cleanString(onGoodsRelevantResponse.getOwnerID());

		GoodStateResponse onOwnershipRelevantResponse = writeBackMessage.getHighestOwnershipState();
		String onOwnershipGoodID = InputValidation.cleanString(onGoodsRelevantResponse.getGoodID());
		String onOwnershipOwnerID = InputValidation.cleanString(onOwnershipRelevantResponse.getOwnerID());

		if (!onGoodsGoodID.equals(onOwnershipGoodID) || !onGoodsOwnerID.equals(onOwnershipOwnerID)) {
			throw new FailedWriteBackException("The GoodID or OwnerID of the GoodStateResponses did not match.");
		}

		long onGoodsWts = onGoodsRelevantResponse.getOnGoodsWts();
		boolean onSale = onGoodsRelevantResponse.isOnSale();
		String writeOnGoodsSignature = onGoodsRelevantResponse.getWriteOnGoodsOperationSignature();
		res = verifyWriteOnGoodsOperationSignature(onGoodsGoodID, onSale, onGoodsOwnerID, onGoodsWts, writeOnGoodsSignature);
		if (!res) {
			throw new SignatureException("The Write On Goods Operation's signature is not valid.");
		}

		long onOwnershipWts = onOwnershipRelevantResponse.getOnOwnershipWts();
		String writeOnOwnershipsSignature = onOwnershipRelevantResponse.getWriteOnOwnershipOperationSignature();
		res = verifyWriteOnOwnershipSignature(onOwnershipGoodID, onOwnershipOwnerID, onOwnershipWts, writeOnOwnershipsSignature);
		if (!res) {
			throw new SignatureException("The Write On Ownership Operation's signature is not valid.");
		}

		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			long databaseOnOwnershipWriteTimestamp = getOnOwnershipTimestamp(connection, onOwnershipGoodID);
			if (isOneTimestampAfterAnother(onOwnershipWts, databaseOnOwnershipWriteTimestamp)) {
				changeGoodOwner(connection, onOwnershipGoodID, onOwnershipOwnerID, ""+onOwnershipWts, writeOnOwnershipsSignature);
			}

			long databaseOnGoodsWriteTimestamp = getOnGoodsTimestamp(connection, onGoodsGoodID);
			if (isOneTimestampAfterAnother(onGoodsWts, databaseOnGoodsWriteTimestamp)) {
				changeGoodSaleStatus(connection, onGoodsGoodID, onSale, onGoodsOwnerID, ""+onGoodsWts, writeOnGoodsSignature);
			}

			connection.commit();
			BasicMessage payload = new WriteBackResponse(generateTimestamp(), writeBackMessage.getRequestID(), OPERATION,
										FROM_SERVER, writeBackMessage.getFrom(), "", writeBackMessage.getRid());
			return new MetaResponse(payload);
		}
		catch (Exception ex) {
			if (connection != null) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw ex; // Handled in writeBack's main method, in the try catch were execute is called.
		}
	}
}
