package hds.server.helpers;

import hds.server.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
	private static DataSource dataSource;

	private DatabaseManager() {
		// This is here so the class can't be instantiated. //
	}

	public static Connection getConnection() throws SQLException {
		// TODO - Improve error handling. //
		if (dataSource == null) {
			dataSource = DatabaseConfig.dataSource();
		}
		return dataSource.getConnection();
	}
}
