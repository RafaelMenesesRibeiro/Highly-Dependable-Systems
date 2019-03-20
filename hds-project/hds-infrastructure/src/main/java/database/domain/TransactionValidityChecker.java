package database.domain;

import database.exception.*;

import java.sql.Connection;
import java.util.List;

class TransactionValidityChecker {

	static String getCurrentOwner(Connection conn, String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException {
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'getCurrentOwner' is either null or an empty string.");
		}
		String query = "select (ownership.userId) from ownership where ownership.goodId = '" + goodID + "'";
		try {
			List<String> results = DatabaseInterface.QueryDB(conn, query, "userId");
			return results.get(0);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}

	static Boolean getIsOnSale(Connection conn, String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException {
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'getIsOnSale' is either null or an empty string.");
		}
		String query = "select (goods.onSale) from goods where goods.goodId = '" + goodID + "'";
		try {
			List<String> results = DatabaseInterface.QueryDB(conn, query, "onSale");
			return results.get(0).equals("t");
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}
}
