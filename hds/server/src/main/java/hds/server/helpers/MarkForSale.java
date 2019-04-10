package hds.server.helpers;

import hds.server.exception.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class MarkForSale {

	private MarkForSale() {
		// This is here so the class can't be instantiated. //
	}

	public static void markForSale(Connection conn, String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException {
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'markForSale' is either null or an empty string.");
		}
		String query = "update goods set onSale = ? where goods.goodID = ?";
		List<String> args = new ArrayList<>();
		args.add("true");
		args.add(goodID);
		try {
			DatabaseInterface.queryDB(conn, query, "", args);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}
}
