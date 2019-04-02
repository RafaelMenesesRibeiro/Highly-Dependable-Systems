package com.hds.helpers;

import com.hds.exception.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class TransferGood {

	public static void TransferGood(Connection conn, String sellerID, String buyerID, String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException {

		if (sellerID == null || sellerID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'sellerID' in query 'transferGood' is either null or an empty string.");
		}
		if (buyerID == null || buyerID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'buyerID' in query 'transferGood' is either null or an empty string.");
		}
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'transferGood' is either null or an empty string.");
		}

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
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException | DBNoResultsException
		// are ignored to be caught up the chain.
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}
}
