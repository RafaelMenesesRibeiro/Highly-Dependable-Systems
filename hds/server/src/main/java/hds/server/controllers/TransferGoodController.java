package hds.server.controllers;

import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.*;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.ChallengeData;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.helpers.TransferGood;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static hds.security.DateUtils.*;
import static hds.security.SecurityManager.verifyWriteOnGoodsOperationSignature;
import static hds.security.SecurityManager.verifyWriteOnOwnershipSignature;
import static hds.server.controllers.controllerHelpers.GeneralControllerHelper.removeAndReturnChallenge;
import static hds.server.controllers.controllerHelpers.GeneralControllerHelper.tryGetUnansweredChallenge;
import static hds.server.helpers.TransactionValidityChecker.getOnOwnershipTimestamp;
import static hds.server.helpers.TransactionValidityChecker.isClientWilling;

/**
 * Responsible for handling POST requests for the endpoint /transferGood.
 * Confirms authenticity and integrity of the request and wrapped request.
 * Confirms the SellerID owns the GoodID.
 * Confirms the GoodID is on sale.
 * Transfers a GoodID from the SellerID to the BuyerID.
 *
 * @author 		Rafael Ribeiro
 * @see 		ApproveSaleRequestMessage
 */
@RestController
public class TransferGoodController extends BaseController {
	private static final String CERTIFIED = "Certified by Notary";

	public TransferGoodController() {
		OPERATION = "transferGood";
	}

	/**
	 * REST Controller responsible for transferring a GoodID.
	 *
	 * @param 	transactionData	GoodID, BuyerID and SellerID
	 * @param 	result    		result of validators for inputs of transactionData
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see		ApproveSaleRequestMessage
	 * @see 	BindingResult
	 */
	@PostMapping(value = "/transferGood",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> transferGood(@RequestBody @Valid ApproveSaleRequestMessage transactionData, BindingResult result) {
		return GeneralControllerHelper.generalControllerSetup(transactionData, result, this);
	}

	/**
	 * Confirms authenticity and integrity of the request and wrapped request.
	 * Confirms the SellerID owns the GoodID.
	 * Confirms the GoodID is on sale.
	 * Transfers a GoodID from the SellerID to the BuyerID.
	 *
	 * @param 	requestData			ApproveSaleRequestMessage
	 * @return 	MetaResponse 		Contains an HttpStatus code and a BasicMessage
	 * @throws 	JSONException					Can't create / parse JSONObject
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @see 	ApproveSaleRequestMessage
	 * @see 	SaleCertificateResponse
	 * @see 	MetaResponse
	 */
	@Override
	public MetaResponse execute(BasicMessage requestData)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException,
			DBNoResultsException, OldMessageException, NoPermissionException {

		ApproveSaleRequestMessage transactionData = (ApproveSaleRequestMessage) requestData;

		String buyerID = InputValidation.cleanString(transactionData.getBuyerID());
		String sellerID = InputValidation.cleanString(transactionData.getSellerID());
		String goodID = InputValidation.cleanString(transactionData.getGoodID());

		if (buyerID.equals(sellerID)) {
			throw new BadTransactionException("BuyerID cannot be equal to SellerID " + buyerID);
		}

		UserRequestIDKey key = new UserRequestIDKey(transactionData.getWrappingFrom(), transactionData.getRequestID());
		ChallengeData challengeData = removeAndReturnChallenge(key);
		String challengeResponse = transactionData.getChallengeResponse();
		if (challengeData == null || !challengeData.verify(challengeResponse)) {
			throw new ChallengeFailedException("The response " + challengeResponse + " was wrong.");
		}

		String wrappingSignature = transactionData.getWrappingSignature();
		transactionData.setWrappingSignature("");
		if (!isClientWilling(sellerID, wrappingSignature, transactionData)) {
			throw new IncorrectSignatureException("The Seller's signature is not valid.");
		}
		transactionData.setWrappingSignature(wrappingSignature);

		long requestWriteTimestamp = transactionData.getWts();
		String writeOnOwnershipsSignature = transactionData.getWriteOnOwnershipsSignature();
		boolean res = verifyWriteOnOwnershipSignature(goodID, buyerID, requestWriteTimestamp, writeOnOwnershipsSignature);
		if (!res) {
			throw new SignatureException("The Write On Ownership Operation's signature is not valid.");
		}

		String writeOnGoodsSignature = transactionData.getWriteOnGoodsSignature();
		res = verifyWriteOnGoodsOperationSignature(goodID, transactionData.getOnSale(), buyerID, requestWriteTimestamp, writeOnGoodsSignature);
		if (!res) {
			throw new SignatureException("The Write On Goods Operation's signature is not valid.");
		}

		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			long databaseWriteTimestamp = getOnOwnershipTimestamp(connection, goodID);
			if (!isOneTimestampAfterAnother(requestWriteTimestamp, databaseWriteTimestamp)) {
				connection.rollback();
				throw new OldMessageException("Write Timestamp " + requestWriteTimestamp + " is too old");
			}

			if (!TransactionValidityChecker.isValidTransaction(connection, transactionData)) {
				connection.rollback();
				throw new BadTransactionException("The transaction is not valid.");
			}

			TransferGood.transferGood(connection, goodID, buyerID, ""+requestWriteTimestamp, writeOnOwnershipsSignature, writeOnGoodsSignature);
			connection.commit();
			connection.close();
			SaleCertificateResponse payload = new SaleCertificateResponse(
					generateTimestamp(),
					transactionData.getRequestID(),
					OPERATION,
					FROM_SERVER,
					transactionData.getFrom(),
					"",
					CERTIFIED,
					goodID,
					sellerID,
					buyerID,
					transactionData.getWts());
			return new MetaResponse(payload);
		}
		catch (Exception ex) {
			if (connection != null) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
			throw ex; // Handled in transferGood's main method, in the try catch were execute is called.
		}
	}
}
