package hds.server.helpers;

import hds.server.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gets the database connection.
 *
 * @author 		Rafael Ribeiro
 */
public class DatabaseManager {
	private static DataSource dataSource;

	private DatabaseManager() {
		// This is here so the class can't be instantiated. //
	}

	/**
	 * Gets the database connection.
	 *
	 * @returns	Connection	Connection to the database
	 */
	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			dataSource = DatabaseConfig.dataSource();
		}
		return dataSource.getConnection();
	}
}
