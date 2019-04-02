package com.hds.helpers;

import com.hds.exception.DBConnectionRefusedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {

	private DatabaseManager() {
		// This is here so the class can't be instantiated. //
	}

	public static Connection getConnection() throws SQLException {
		String dbURL = System.getenv("JDBC_DATABASE_URL");
		try {
			return DriverManager.getConnection(dbURL);
		}
		catch (SQLException sqlex) {
			Logger logger = Logger.getAnonymousLogger();
			switch (sqlex.getSQLState()) {
				case "3D000":
					logger.warning("Error trying to connect to database. The database at '" + dbURL + "' does not exist.\n");
					System.exit(1);
					break;
				case "28P01":
					logger.warning("Error trying to connect to database. Either username or password are wrong.\n");
					System.exit(1);
					break;
				case "08001":
					logger.warning("Error trying to connect to database. Connection to '" + dbURL + "' was refused.\n");
					System.exit(1);
					break;
				default:
					throw sqlex;
			}
			return null;
		}
	}

	public static Connection getJDBCConnection() throws URISyntaxException, SQLException, DBConnectionRefusedException {
		URI dbURI = new URI("");
		String username = "";
		String password = "";
		try {
			dbURI = new URI(System.getenv("DATABASE_URL"));

			username = dbURI.getUserInfo().split(":")[0];
			password = dbURI.getUserInfo().split(":")[1];
			String dbUrl = "jdbc:postgresql://" + dbURI.getHost() + ':' + dbURI.getPort() + dbURI.getPath() + "?sslmode=require";

			return DriverManager.getConnection(dbUrl, username, password);
		}
		catch (URISyntaxException urisex) {
			throw urisex;
		}
		catch (SQLException sqlex) {
			switch (sqlex.getSQLState()) {
				case "3D000":
					throw new DBConnectionRefusedException("Error trying to connect to database. The database at '" + dbURI.toString() + "' does not exist.\n");
				case "28P01":
					throw new DBConnectionRefusedException("Error trying to connect to database. Either username '" + username + "' or password '" + password + "' are wrong.\n");
				case "08001":
					throw new DBConnectionRefusedException("Error trying to connect to database. Connection to '" + dbURI.toString() + "' was refused.\n");
				default:
					throw sqlex;
			}
		}
	}
}
