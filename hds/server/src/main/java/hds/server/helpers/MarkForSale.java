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
public class MarkForSale {

	private MarkForSale() {
		// This is here so the class can't be instantiated. //
	}

	/**
	 * Marks a GoodID for sale in the database.
	 *
	 * @param 	conn	Database connection
	 * @param 	goodID	GoodID to be marked for sale
	 * @throws  SQLException                    The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public static void markForSale(Connection conn, String goodID)
			throws SQLException, DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException {

		String query = "update goods set onSale = ? where goods.goodID = ?";
		List<String> args = new ArrayList<>();
		args.add("true");
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
