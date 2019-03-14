package database.domain;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Database {
	private final String populationFilename = "populate.sql";

	private String endpoint;
	private String name;
	private String username;
	private String password;

	public Database() {
		FetchProperties("aws-rds-db.properties");
		System.out.println(String.format("Database instance %s created. Endpoint is %s.", name, endpoint));
	}

	private void FetchProperties(String propertiesFilename) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = getClass().getClassLoader().getResourceAsStream(propertiesFilename);
			if (input == null) {
				System.out.println("Unable to read properties file: " + propertiesFilename);
				return;
			}
			properties.load(input);
			endpoint = properties.getProperty("dbEndpoint");
			name = properties.getProperty("dbName");
			username = properties.getProperty("dbUsername");
			password = properties.getProperty("dbPassword");
		}
		catch (IOException ex) { ex.printStackTrace(); }
		finally {
			if (input != null) {
				try { input.close(); }
				catch (IOException e) { e.printStackTrace(); }
			}
		}
	}

	public Connection connecToDB() {
		String url = String.format("jdbc:postgresql:%s/%s", this.endpoint, this.name);

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, this.username, this.password);
			System.out.println("Connected to the PostgreSQL server successfully.");
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}

	public void QueryDB(Connection conn, String query) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			System.out.println("Results:");
			while(rs.next()) {
				System.out.println(String.format("RESULT: %s", rs.getString("userid")));
			}
			conn.close();
			stmt.close();
			rs.close();
		}
		catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void PopulateDB(String populationFilename) {
		
	}
}
