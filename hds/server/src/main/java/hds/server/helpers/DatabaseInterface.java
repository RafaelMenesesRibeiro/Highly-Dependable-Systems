package hds.server.helpers;

import hds.server.exception.DBClosedConnectionException;
import hds.server.exception.DBConnectionRefusedException;
import hds.server.exception.DBNoResultsException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class DatabaseInterface {

	private DatabaseInterface() {
		// This is here so the class can't be instantiated. //
	}

	static List<String> queryDB(Connection conn, String query, String returnColumn, List<String> args)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBNoResultsException, SQLException {

		List<String> results = new ArrayList<>();
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
			while(rs.next()) {
				results.add(rs.getString(returnColumn));
			}
			rs.close();
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
					if (!returnColumn.equals("")) {
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
