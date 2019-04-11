package hds.server.helpers;

import hds.security.ConvertUtils;
import hds.security.CryptoUtils;
import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.server.exception.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static hds.security.ResourceManager.*;

public class TransactionValidityChecker {
	private TransactionValidityChecker() {
		// This is here so the class can't be instantiated. //
	}

	public static boolean isValidTransaction(Connection conn, ApproveSaleRequestMessage transactionData)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException,
					IOException, SignatureException, IncorrectSignatureException {

		String buyerID = transactionData.getBuyerID();
		String sellerID = transactionData.getSellerID();
		String goodID = transactionData.getGoodID();
		byte[] payloadBytes = ConvertUtils.objectToByteArray(transactionData);

		if (!isClientWilling(buyerID, transactionData.getSignature(), transactionData)) {
			throw new IncorrectSignatureException("The Buyer's signature is not valid.");
		}
		if (!isClientWilling(sellerID, transactionData.getWrappingSignature(), transactionData)) {
			throw new IncorrectSignatureException("The Seller's signature is not valid.");
		}

		String currentOwner = getCurrentOwner(conn, goodID);
		if (!currentOwner.equals(sellerID)) { return false; }
		if (!getIsOnSale(conn, goodID)) { return false; }

		return true;
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
			return CryptoUtils.authenticateSignature(buyerPublicKey, buyerSignature, payload);
		}
		catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | java.security.SignatureException e) {
			throw new SignatureException(e.getMessage());
		}
	}
}
