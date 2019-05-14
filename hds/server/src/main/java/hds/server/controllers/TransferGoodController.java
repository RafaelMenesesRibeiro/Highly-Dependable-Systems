package hds.server.controllers;

import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.SaleCertificateResponse;
import hds.server.ServerApplication;
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

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.verifyWriteOnGoodsOperationSignature;
import static hds.security.SecurityManager.verifyWriteOnOwnershipSignature;
import static hds.server.controllers.controllerHelpers.GeneralControllerHelper.removeAndReturnChallenge;
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

		long rcvWts = transactionData.getWts();
		String writeOnOwnershipsSignature = transactionData.getWriteOnOwnershipsSignature();
		boolean res = verifyWriteOnOwnershipSignature(goodID, buyerID, rcvWts, writeOnOwnershipsSignature);
		if (!res) {
			throw new SignatureException("The Write On Ownership Operation's signature is not valid.");
		}

		String writeOnGoodsSignature = transactionData.getWriteOnGoodsSignature();
		res = verifyWriteOnGoodsOperationSignature(goodID, transactionData.getOnSale(), buyerID, rcvWts, writeOnGoodsSignature);
		if (!res) {
			throw new SignatureException("The Write On Goods Operation's signature is not valid.");
		}

		Connection connection = null;
		try {
			connection = DatabaseManager.getConnection();
			connection.setAutoCommit(false);

			// TODO - Check this. //
			/*
				The timestamp is not verified against the one in Goods table, is it will be replaced regardless.
				The only problem is it might break the property of the safety (more specifically ordering).
				That would be the case when a client marks a good for sale (with ts=t1) immediately after a client
				requests that same seller with buyGood. When it requests, it generates an entry for the Goods table
				with "onSale"=false and, in this case, ts=t2.
				If a client then reads the state of good it will receive "onSale"=true and ts=t2.
				After the sale, the Goods table is updated with the entry sent by the buyer ("onSale"=false, ts=t2)
				If a client calls GetStateOfGood, it will read "onSale"=false, ts=2.
				The problem becomes apparent because t2<t1. Therefore, a read v, that came after read w, returned
				a value that was written before the one read by w.
			 */

			if (!TransactionValidityChecker.isValidTransaction(connection, transactionData)) {
				connection.rollback();
				throw new BadTransactionException("The transaction is not valid.");
			}

			synchronized (this) {
				// TODO - Add same verification up top to be able to refuse faster.
				// Leave this one here in case someone writes before this.
				int myWts = ServerApplication.getCurrentWriteTimestamp();

				if (!(rcvWts > myWts)) {
					throw new OldMessageException("Write timestamp " + rcvWts + " is too old.");
				}
				TransferGood.transferGood(connection, goodID, buyerID, ""+ rcvWts, writeOnOwnershipsSignature, writeOnGoodsSignature);
			}

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
