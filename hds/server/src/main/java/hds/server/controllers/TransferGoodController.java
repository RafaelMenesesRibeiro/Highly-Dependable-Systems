package hds.server.controllers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.TransactionValidityChecker;
import hds.server.helpers.TransferGood;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@RestController
public class TransferGoodController {
	private static final String FROM_SERVER = "server";
	private static final String OPERATION = "transferGood";

	@PostMapping(value = "/transferGood",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> transferGood(@RequestBody ApproveSaleRequestMessage transactionData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Transfer Good request.");

		String buyerID = InputValidation.cleanString(transactionData.getBuyerID());
		String sellerID = InputValidation.cleanString(transactionData.getSellerID());
		String goodID = InputValidation.cleanString(transactionData.getGoodID());
		logger.info("\tBuyerID - " + buyerID);
		logger.info("\tSellerID - " + sellerID);
		logger.info("\tGoodID - " + goodID);
		MetaResponse metaResponse;
		try {
			InputValidation.isValidClientID(sellerID, "sellerID");
			InputValidation.isValidClientID(buyerID, "sellerID");
			InputValidation.isValidGoodID(goodID);
			metaResponse = execute(transactionData);
		}
		catch (IOException ioex) {
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.CANCER, ioex.getMessage());
			metaResponse = new MetaResponse(403, payload);
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
		}
		return GeneralControllerHelper.getResponseEntity(metaResponse, transactionData.getRequestID(), transactionData.getFrom(), OPERATION);
	}

	private MetaResponse execute(ApproveSaleRequestMessage transactionData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBSQLException,
					InvalidQueryParameterException, DBNoResultsException, IOException {

		String buyerID = transactionData.getBuyerID();
		String sellerID = transactionData.getSellerID();
		String goodID = transactionData.getGoodID();

		try (Connection conn = DatabaseManager.getConnection()) {
			if (TransactionValidityChecker.isValidTransaction(conn, transactionData)) {
				TransferGood.transferGood(conn, sellerID, buyerID, goodID);
				BasicMessage payload = new BasicMessage(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "");
				return new MetaResponse(payload);
			}
			else {
				String reason = "The transaction is not valid.";
				ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_TRANSACTION, reason);
				return new MetaResponse(403, payload);
			}
		}
		catch (IncorrectSignatureException is){
			ErrorResponse payload = new ErrorResponse(transactionData.getRequestID(), OPERATION, FROM_SERVER, transactionData.getFrom(), "", ControllerErrorConsts.BAD_TRANSACTION, is.getMessage());
			return new MetaResponse(403, payload);
		}
	}
}
