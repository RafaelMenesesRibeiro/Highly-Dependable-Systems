package hds.server.helpers;

import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for querying the database.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
class DatabaseInterface {

	private DatabaseInterface() {
		// This is here so the class can't be instantiated. //
	}

	/**
	 * Queries the database and returns a List of JSONObjects. Each JSONObject corresponds to one result and is
	 * comprised of column name (as key) and respective query returned value for that column (value).
	 *
	 * @param 	conn         	Database connection
	 * @param 	query        	Query to be transformed into a PreparedStatement
	 * @param 	returnColumns 	Return columns for the query
	 * @param 	args         	Arguments of the PreparedStatement
	 * @return 	List		 	Represents the list of the query's results
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 * @see 	Connection
	 * @see 	PreparedStatement
	 */
	static List<JSONObject> queryDB(Connection conn, String query, List<String> returnColumns, List<String> args)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException, SQLException, JSONException {

		List<JSONObject> results = new ArrayList<>();
		try (
				PreparedStatement stmt = conn.prepareStatement(query)) {

			int i = 1;
			for (String s: args) {
				if (s.equals("false")) {
					stmt.setBoolean(i, false);
				}
				else if (s.equals("true")) {
					stmt.setBoolean(i, true);
				}
				else {
					stmt.setString(i, s);
				}
				i += 1;
			}

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				JSONObject json = new JSONObject();
				for (String key: returnColumns) {
					json.put(key, rs.getString(key));
				}
				results.add(json);
			}
			rs.close();
		}
		catch (IndexOutOfBoundsException | NullPointerException ex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
		catch (SQLTimeoutException sqltex) {
			throw new DBNoResultsException(sqltex.getMessage());
		}
		catch (SQLException sqlex) {
			switch (sqlex.getSQLState()) {
				case "08000": // connection_exception
				case "08003": // connection_does_not_exist
				case "08006": // connection_failure
				case "08001": // qlclient_unable_to_establish_sqlconnection
					throw new DBClosedConnectionException(sqlex.getMessage());
				case "08004": // sqlserver_rejected_establishment_of_sqlconnection
				case "08007": // transaction_resolution_unknown
				case "08P01": // protocol_violation
					throw new DBConnectionRefusedException(sqlex.getMessage());
				case "02000": // no_data
					if (!returnColumns.isEmpty()) {
						throw new DBNoResultsException(sqlex.getMessage());
					}
					break;
				default:
					throw sqlex;
			}
		}
		return results;
	}
}
