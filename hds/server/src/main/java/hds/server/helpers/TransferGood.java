package hds.server.helpers;

import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;
import org.json.JSONException;

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
	 * @see 	MarkForSale
	 */
	public static void transferGood(Connection conn,
									final String goodID, final String writerID,
									final String writeTimestamp, final String writeOnOwnershipSignature,
									final String writeOnGoodsSignature)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {


		MarkForSale.changeGoodSaleStatus(conn, goodID, false, writerID, writeTimestamp, writeOnGoodsSignature);

		String query = "UPDATE ownership " +
				"SET userID = ?, " +
				"ts = ?, " +
				"sig = ?" +
				"WHERE goodID = ?";
		List<String> args = new ArrayList<>();
		args.add(writerID);
		args.add(writeTimestamp);
		args.add(writeOnOwnershipSignature);
		args.add(goodID);

		List<String> returnColumns = new ArrayList<>();

		try {
			DatabaseInterface.queryDB(conn, query, returnColumns, args);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}

	/**
	 * Changes the GoodID's owner.
	 *
	 * @param 	connection			Database connection
	 * @param 	goodID				GoodID to be transferred
	 * @param   newOwner        	ID of the GoodID's new owner
	 * @param 	writeTimestamp  	Writer's own write Logic timestamp. Identifies if this writing is relevant
	 * @param	writeOnOwnershipSignature 	Signature for the write on ownership operation
	 * @throws 	JSONException					Can't create / parse JSONObject
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @see 	MarkForSale
	 */
	public  static void changeGoodOwner(Connection connection, final String goodID, final String newOwner,
										final String writeTimestamp, final String writeOnOwnershipSignature)
			throws SQLException, JSONException {

		String query = "UPDATE ownership " +
				"SET userID = ?, " +
				"ts = ?, " +
				"sig = ?" +
				"WHERE goodID = ?";
		List<String> args = new ArrayList<>();
		args.add(newOwner);
		args.add(writeTimestamp);
		args.add(writeOnOwnershipSignature);
		args.add(goodID);

		List<String> returnColumns = new ArrayList<>();

		try {
			DatabaseInterface.queryDB(connection, query, returnColumns, args);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}
}
