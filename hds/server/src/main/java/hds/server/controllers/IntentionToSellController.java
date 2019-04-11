package hds.server.controllers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.OwnerDataMessage;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.security.InputValidation;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.MarkForSale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static hds.server.helpers.TransactionValidityChecker.getCurrentOwner;
import static hds.server.helpers.TransactionValidityChecker.isClientWilling;

@RestController
public class IntentionToSellController {
	private static final String FROM_SERVER = "server";
	private static final String OPERATION = "markForSale";

	@PostMapping(value = "/intentionToSell",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> intentionToSell(@RequestBody OwnerDataMessage ownerData) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Intention to Sell request.");

		String sellerID = InputValidation.cleanString(ownerData.getOwner());
		String goodID = InputValidation.cleanString(ownerData.getGoodID());
		logger.info("\tSellerID - " + sellerID);
		logger.info("\tGoodID - " + goodID);
		MetaResponse metaResponse;
		try {
			InputValidation.isValidClientID(sellerID, "sellerID");
			InputValidation.isValidGoodID(goodID);
			metaResponse = execute(ownerData);
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
		}
		return GeneralControllerHelper.getResponseEntity(metaResponse, ownerData.getRequestID(), ownerData.getFrom(), OPERATION);
	}

	private MetaResponse execute(OwnerDataMessage ownerData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException,
					DBSQLException, InvalidQueryParameterException, DBNoResultsException {

		String sellerID = ownerData.getOwner();
		String goodID = ownerData.getGoodID();

		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);
			String ownerID = getCurrentOwner(conn, goodID);
			if (!ownerID.equals(sellerID)) {
				conn.rollback();
				String reason = "The user '" + sellerID + "' does not own the good '" + goodID + "'.";
				ErrorResponse payload = new ErrorResponse(ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ControllerErrorConsts.NO_PERMISSION, reason);
				return new MetaResponse(403, payload);
			}
			boolean res = isClientWilling(sellerID, ownerData.getSignature(), ownerData);
			if (!res) {
				conn.rollback();
				String reason = "The Seller's signature is not valid.";
				ErrorResponse payload = new ErrorResponse(ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ControllerErrorConsts.BAD_TRANSACTION, reason);
				return new MetaResponse(403, payload);
			}
			MarkForSale.markForSale(conn, goodID);
			conn.commit();
			BasicMessage payload = new BasicMessage(ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "");
			return new MetaResponse(payload);
		}
		catch (SignatureException is){
			if (conn != null) {
				conn.rollback();
			}
			ErrorResponse payload = new ErrorResponse(ownerData.getRequestID(), OPERATION, FROM_SERVER, ownerData.getFrom(), "", ControllerErrorConsts.BAD_TRANSACTION, is.getMessage());
			return new MetaResponse(403, payload);
		}
		catch (SQLException | DBSQLException | DBNoResultsException ex) {
			if (conn != null) {
				conn.rollback();
			}
			throw ex;
		}
		finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}