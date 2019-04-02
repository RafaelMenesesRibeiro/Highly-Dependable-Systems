package hds.server.helpers;

import hds.server.DatabaseConfig;
import hds.server.exception.DBConnectionRefusedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseManager {
	@Autowired
	private ApplicationContext appContext;

	private DatabaseManager() {
		// This is here so the class can't be instantiated. //
	}

	public static Connection getConnection() throws SQLException {
		return new DatabaseConfig().dataSource().getConnection();
	}
}
