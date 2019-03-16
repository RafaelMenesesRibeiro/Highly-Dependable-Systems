package database.domain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.exception.DBClosedConnectionException;
import database.exception.DBConnectionRefusedException;
import database.exception.DBSQLException;

public class DatabaseInterface {

	static Connection connectToDB(String endpoint, String dbName, String username, String password) {
		String url = String.format("jdbc:postgresql:%s/%s", endpoint, dbName);
		try {
			return DriverManager.getConnection(url, username, password);
		}
		catch (SQLException sqlex) {
			switch (sqlex.getSQLState()) {
				case "3D000":
					System.out.println("Error trying to connect to database. The database '" + dbName + "' does not exist.\n");
					System.exit(1);
				case "28P01":
					System.out.println("Error trying to connect to database. Either username or password are wrong in " +
							"properties file 'aws-rds-db.properties'\n");
					System.exit(1);
				case "08001":
					System.out.println("Error trying to connect to database. Connection to '" + endpoint + "' was refused.\n");
					System.exit(1);
			}
		}
		return null;
	}

	static List<String> QueryDB(Connection conn, String query, String returnColumn)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException {
		List<String> results = new ArrayList<String>();
		try {
			// TODO - Create pre prepared statements. //
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				results.add(rs.getString(returnColumn));
			}
			stmt.close();
			rs.close();
		}
		// TODO - Is exiting the best approach for this exception? //
		catch (SQLTimeoutException sqltex) {
			System.out.println(sqltex.getMessage());
			System.exit(1);
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
				case "2F000": // sql_routine_exception
				case "2F003": // prohibited_sql_statement_attempted
				case "42703": // undefined_column
				case "42P01": // undefined_table
					throw new DBSQLException(sqlex.getMessage());
				default:
					System.out.println(sqlex.getSQLState());
					System.out.println(sqlex.getMessage());
					System.exit(1);
			}
		}
		return results;
	}
}
