package hds.server.helpers;

import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Marks a GoodID for sale in the database.
 *
 * @author 		Rafael Ribeiro
 */
@SuppressWarnings("Duplicates")
public class MarkForSale {

	private MarkForSale() {
		// This is here so the class can't be instantiated. //
	}

	/**
	 * Marks a GoodID for sale in the database.
	 *
	 * @param 	conn			Database connection
	 * @param 	goodID			GoodID to be marked for sale
	 * @param 	writerID    	ID of the client responsible for the writing
	 *                         	(in this context, it's always the owner)
	 * @param 	writeTimestamp  Request's write Operation timestamp. Identifies if this writing is relevant
	 * @param	writeOperationSignature Signature for the write operation
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public static void markForSale(Connection conn, String goodID, String writerID, String writeTimestamp, String writeOperationSignature)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		String query = "UPDATE goods " +
				"SET onSale = ?, " +
					"wid = ?, " +
					"ts = ?, " +
					"sig = ? " +
				"WHERE goods.goodID = ?";
		List<String> args = new ArrayList<>();
		args.add("true");
		args.add(writerID);
		args.add(writeTimestamp);
		args.add(writeOperationSignature);
		args.add(goodID);
		try {
			DatabaseInterface.queryDB(conn, query, "", args);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}
}
