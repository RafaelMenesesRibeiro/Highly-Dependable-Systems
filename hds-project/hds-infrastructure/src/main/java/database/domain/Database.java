package database.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Database {
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
}
