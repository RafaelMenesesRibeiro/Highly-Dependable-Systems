package hds.server.helpers;

import hds.security.CryptoUtils;
import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.SaleRequestMessage;
import hds.server.exception.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static hds.security.ResourceManager.getPublicKeyFromResource;

public class TransactionValidityChecker {
	private TransactionValidityChecker() {
		// This is here so the class can't be instantiated. //
	}

	public static boolean isValidTransaction(Connection conn, ApproveSaleRequestMessage transactionData)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException,
					SignatureException, IncorrectSignatureException {

		String buyerID = transactionData.getBuyerID();
		String sellerID = transactionData.getSellerID();
		String goodID = transactionData.getGoodID();

		SaleRequestMessage saleRequestMessage = new SaleRequestMessage(
				transactionData.getTimestamp(),
				transactionData.getRequestID(),
				transactionData.getOperation(),
				transactionData.getFrom(),
				transactionData.getTo(),
				"",
				transactionData.getGoodID(),
				transactionData.getBuyerID(),
				transactionData.getSellerID()
		);

		if (!isClientWilling(buyerID, transactionData.getSignature(), saleRequestMessage)) {
			throw new IncorrectSignatureException("The Buyer's signature is not valid.");
		}

		String wrappingSignature = transactionData.getWrappingSignature();
		transactionData.setWrappingSignature("");
		if (!isClientWilling(sellerID, wrappingSignature, transactionData)) {
			throw new IncorrectSignatureException("The Seller's signature is not valid.");
		}
		transactionData.setWrappingSignature(wrappingSignature);

		String currentOwner = getCurrentOwner(conn, goodID);
		return (currentOwner.equals(sellerID) && getIsOnSale(conn, goodID));
	}

	public static String getCurrentOwner(Connection conn, String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, DBNoResultsException {

		String query = "select userID from ownership where goodId = ?";
		List<String> args = new ArrayList<>();
		args.add(goodID);
		try {
			List<String> results = DatabaseInterface.queryDB(conn, query, "userID", args);
			return results.get(0);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}

	public static Boolean getIsOnSale(Connection conn, String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, DBNoResultsException {

		String query = "select onSale from goods where goodID = ?";
		List<String> args = new ArrayList<>();
		args.add(goodID);
		try {
			List<String> results = DatabaseInterface.queryDB(conn, query, "onSale", args);
			return results.get(0).equals("t");
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}

	public static boolean isClientWilling(String clientID, String buyerSignature, Object payload)
			throws SignatureException {
		try {
			PublicKey buyerPublicKey = getPublicKeyFromResource(clientID);
			return CryptoUtils.authenticateSignatureWithPubKey(buyerPublicKey, buyerSignature, payload.toString());
		}
		catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | java.security.SignatureException e) {
			throw new SignatureException(e.getMessage());
		}
	}
}
