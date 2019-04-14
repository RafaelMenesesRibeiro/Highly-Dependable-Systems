package hds.server.helpers;

import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transfers a GoodID from the SellerID to the BuyerID
 *
 * @author 		Rafael Ribeiro
 */
public class TransferGood {
	private TransferGood() {
		// This is here so the class can't be instantiated. //
	}

	/**
	 * Marks a GoodID for sale in the database.
	 *
	 * @param 	conn		Database connection
	 * @param 	sellerID	SellerID
	 * @param 	buyerID		BuyerID
	 * @param 	goodID		GoodID to be transferred
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public static void transferGood(Connection conn, String sellerID, String buyerID, String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		String query = "UPDATE goods SET onSale = ? WHERE goodID = ?";
		List<String> args = new ArrayList<>();
		args.add("false");
		args.add(goodID);

		String query2 = "UPDATE ownership SET userID = ? WHERE goodID = ?";
		List<String> args2 = new ArrayList<>();
		args2.add(buyerID);
		args2.add(goodID);

		try {
			DatabaseInterface.queryDB(conn, query, "", args);
			DatabaseInterface.queryDB(conn, query2, "", args2);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}
}
