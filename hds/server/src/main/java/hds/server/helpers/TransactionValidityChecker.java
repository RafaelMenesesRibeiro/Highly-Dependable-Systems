package hds.server.helpers;

import hds.security.domain.SignedTransactionData;
import hds.security.domain.TransactionData;
import hds.server.exception.*;

import java.io.IOException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static hds.security.SecurityManager.*;

public class TransactionValidityChecker {
	private TransactionValidityChecker() {
		// This is here so the class can't be instantiated. //
	}

	public static boolean isValidTransaction(Connection conn, SignedTransactionData signedData)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException,
					IOException, SignatureException, IncorrectSignatureException {

		TransactionData transactionData = signedData.getPayload();
		String buyerID = transactionData.getBuyerID();
		String sellerID = transactionData.getSellerID();
		String goodID = transactionData.getGoodID();
		byte[] payloadBytes = getByteArray(transactionData);

		if (!isClientWilling(buyerID, signedData.getBuyerSignature(), payloadBytes)) {
			throw new IncorrectSignatureException("The Buyer's signature is not valid.");
		}
		if (!isClientWilling(sellerID, signedData.getSellerSignature(), payloadBytes)) {
			throw new IncorrectSignatureException("The Seller's signature is not valid.");
		}

		String currentOwner = getCurrentOwner(conn, goodID);
		if (!currentOwner.equals(sellerID)) { return false; }
		if (!getIsOnSale(conn, goodID)) { return false; }

		return true;
	}

	public static String getCurrentOwner(Connection conn, String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException, DBNoResultsException {
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'getCurrentOwner' is either null or an empty string.");
		}
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
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException, DBNoResultsException {
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'getIsOnSale' is either null or an empty string.");
		}
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

	public static boolean isClientWilling(String clientID, byte[] buyerSignature, byte[] payloadBytes)
			throws SignatureException {
		try {
			PublicKey buyerPublicKey = getPublicKeyFromResource(clientID);
			return verifySignature(buyerPublicKey, buyerSignature, payloadBytes);
		}
		catch (IOException | InvalidKeySpecException e) {
			throw new SignatureException(e.getMessage());
		}
	}
}
