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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static hds.security.ResourceManager.getPublicKeyFromResource;

/**
 * Verifies if the transaction is valid.
 *
 * @author 		Rafael Ribeiro
 */
public class TransactionValidityChecker {
	private TransactionValidityChecker() {
		// This is here so the class can't be instantiated. //
	}

	/**
	 * Verifies if the transaction is valid.
	 * Confirms authenticity and integrity of the request and wrapped request.
	 * Confirms the SellerID owns the GoodID.
	 * Confirms the GoodID is on sale.
	 *
	 * @param   conn        	Database connection
	 * @param 	transactionData	ApproveSaleRequestMessage with GoodID, BuyerID, SellerID and all the signatures
	 * @return 	boolean			Represents if the transaction is valid.
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @throws 	SignatureException				Couldn't verify the payload's signature
	 * @throws	IncorrectSignatureException		The payload's signature does not match its contents
	 */
	public static boolean isValidTransaction(Connection conn, ApproveSaleRequestMessage transactionData)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException,
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

	/**
	 * Gets the current owner of the GoodID.
	 *
	 * @param   conn        Database connection
	 * @param 	goodID		GoodID
	 * @return 	String		ID of the GoodID's owner
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public static String getCurrentOwner(Connection conn, String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		String query = "select userID from ownership where goodId = ?";
		List<String> args = new ArrayList<>();
		args.add(goodID);
		try {
			List<String> results = DatabaseInterface.queryDB(conn, query, "userID", args);
			return results.get(0);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}

	/**
	 * Checks if the GoodID is on sale in the database.
	 *
	 * @param   conn        Database connection
	 * @param 	goodID		GoodID
	 * @return 	Boolean		Represents if the GoodID is on sale
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public static Boolean getIsOnSale(Connection conn, String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		String query = "select onSale from goods where goodID = ?";
		List<String> args = new ArrayList<>();
		args.add(goodID);
		try {
			List<String> results = DatabaseInterface.queryDB(conn, query, "onSale", args);
			return results.get(0).equals("t");
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}

	/**
	 * Verifies the signature of the payload.
	 *
	 * @param 	clientID		The ClientID that sent the payload
	 * @param 	buyerSignature	The Signature's String of the payload
	 * @param 	payload			The payload that was signed
	 * @throws  SignatureException              Couldn't sign the payload
	 */
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
