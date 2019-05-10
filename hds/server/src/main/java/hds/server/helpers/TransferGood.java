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
@SuppressWarnings("Duplicates")
public class TransferGood {
	private TransferGood() {
		// This is here so the class can't be instantiated. //
	}

	/**
	 * Marks a GoodID for sale in the database.
	 *
	 * @param 	conn				Database connection
	 * @param 	goodID				GoodID to be transferred
	 * @param   writerID        	ID of the client responsible for the writing
	 *                              (in this context, it's always the owner)
	 * @param 	writeTimestamp  	Writer's own write Logic timestamp. Identifies if this writing is relevant
	 * @param	writeOnGoodsSignature 		Signature for the write on goods operation
	 * @param	writeOnOwnershipSignature 	Signature for the write on ownership operation
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public static void transferGood(Connection conn,
									final String goodID, final String writerID,
									final String writeTimestamp, final String writeOnOwnershipSignature,
									final String writeOnGoodsSignature)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		// TODO - Refactor MarkForSale to not duplicate this code. //
		String query = "UPDATE goods " +
				"SET onSale = ?, " +
					"wid = ?, " +
					"ts = ?, " +
					"sig = ? " +
				"WHERE goods.goodID = ?";
		List<String> args = new ArrayList<>();
		args.add("false");
		args.add(writerID);
		args.add(writeTimestamp);
		args.add(writeOnGoodsSignature);
		args.add(goodID);

		String query2 = "UPDATE ownership " +
				"SET userID = ?, " +
				"ts = ?, " +
				"sig = ?" +
				"WHERE goodID = ?";
		List<String> args2 = new ArrayList<>();
		args2.add(writerID);
		args2.add(writeTimestamp);
		args2.add(writeOnOwnershipSignature);
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
