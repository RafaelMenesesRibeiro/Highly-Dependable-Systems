package hds.server.controllers;

import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.BasicMessage;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.OldMessageException;
import hds.server.helpers.DatabaseManager;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;

import static hds.security.DateUtils.isOneTimestampAfterAnother;
import static hds.security.SecurityManager.verifyWriteOnGoodsOperationSignature;
import static hds.server.helpers.TransactionValidityChecker.getOnGoodsTimestamp;
import static hds.server.helpers.TransactionValidityChecker.isClientWilling;

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
	 * @param
	 * @param
	 * @return
	 */
	// TODO - Javadoc. //
	@PostMapping(value = "/writeBack",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> writeBack(@RequestBody @Valid BasicMessage writeBackData, BindingResult result) {
		return GeneralControllerHelper.generalControllerSetup(writeBackData, result, this);
	}


	/**
	 * Creates a challenge for the client, and stores it until it responds with a solution encapsulated
	 * in TransferGoodController's received data.
	 *
	 * @param
	 * @return
	 */
	// TODO - Javadoc. //
	@Override
	public MetaResponse execute(BasicMessage requestData) throws SQLException, JSONException {

		String clientID = InputValidation.cleanString(requestData.getFrom());
		String goodID  = "TODO GET"; // InputValidation.cleanString(requestData.getGoodID());

		String signature = requestData.getSignature();
		requestData.setSignature("");
		boolean res = isClientWilling(clientID, signature, requestData);
		requestData.setSignature(signature); // TODO - Is this necessary? //
		if (!res) {
			throw new SignatureException("The Client's signature is not valid.");
		}



		// TODO - Verify WriteOnGoodsOperationSignature. //

		// TODO - Verify WriteOnOwnershipOperationSignature. //

		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			long requestWriteTimestamp = 2; // TODO - requestData.getWriteTimestamp();
			long databaseWriteTimestamp = getOnGoodsTimestamp(connection, goodID);
			if (!isOneTimestampAfterAnother(requestWriteTimestamp, databaseWriteTimestamp)) {
				connection.rollback();
				throw new OldMessageException("Write Back Timestamp " + requestWriteTimestamp + " is too old.");
			}

			// TODO - Write on Goods table. //
			// TODO - Write on Ownership table. //
			// TODO - Send acknowledge. //

		}
		catch (Exception ex) {
			if (connection != null) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw ex; // Handled in writeBack's main method, in the try catch were execute is called.
		}


		return null;
	}
}
