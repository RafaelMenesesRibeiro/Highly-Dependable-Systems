package database.domain;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import database.domain.DatabaseInterface;
import database.exception.*;

public class Database {
	private Connection conn;
	private String endpoint;
	private String name;
	private String username;
	private String password;

	public Database() {
		FetchProperties("aws-rds-db.properties");
		this.conn = DatabaseInterface.connectToDB(this.endpoint, this.name, this.username, this.password);
		System.out.println("Connected to the PostgreSQL server successfully.");

		// TODO - Find out why postgres doesn't create tables with this. //
		//String creationQuery = GetQuery("schemas.sql");
		//QueryDB(conn, creationQuery);
		// TODO - Move to own function and only call once in Main or in PuppetMaster Main. //
		//String populateQuery = GetQuery("populate.sql");
		//QueryDB(conn, populateQuery, "userId");

		System.out.println(String.format("Database instance %s created. Endpoint is %s.", name, endpoint));
	}

	String getEndpoint() {return this.endpoint; }
	String getName() {return this.name; }
	String getUsername() {return this.username; }
	String getPassword() {return this.password; }

	private void FetchProperties(String propertiesFilename) throws InvalidPropertyException {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = getClass().getClassLoader().getResourceAsStream(propertiesFilename);
			if (input == null) {
				throw new IOException("Unable to properties file. Can't access database.\n" +
						"A file called 'aws-rds-db.properties' must exist in src/main/resources.\n" +
						"Follow the instructions of 'aws-rds-db.properties.example' in the same directory.\n" +
						"Exiting.\n");
			}
			properties.load(input);
			endpoint = properties.getProperty("dbEndpoint");
			if (endpoint == null) {throw new InvalidPropertyException("'dbEndpoint' property was read as 'null' in .properties"); }
			name = properties.getProperty("dbName");
			if (name == null) {throw new InvalidPropertyException("'dbName' property was read as 'null' in .properties"); }
			username = properties.getProperty("dbUsername");
			if (username == null) {throw new InvalidPropertyException("'dbUsername' property was read as 'null' in .properties"); }
			password = properties.getProperty("dbPassword");
			if (password == null) {throw new InvalidPropertyException("'dbPassword' property was read as 'null' in .properties"); }
		}
		catch (IOException | InvalidPropertyException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
		finally {
			if (input != null) {
				try { input.close(); }
				catch (IOException ex) {
					System.out.println(ex.getMessage());
					System.exit(1);
				}
			}
		}
	}

	private String GetQuery(String filename) {
		try {
			if (filename == null) {
				throw new NullPointerException("The name of the file containing the query was null.");
			}
			InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				throw new NullPointerException("No file with the given name was found (" + filename + ").");
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName(StandardCharsets.UTF_8.name())));
			StringBuilder data = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				data.append(line);
			}
			return data.toString();
		}
		catch (NullPointerException | IOException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
		return "";
	}

	public String getCurrentOwner(String goodID) {
		try {
			return TransactionValidityChecker.getCurrentOwner(this.conn, goodID);
		}
		catch (DBClosedConnectionException dbccex) {
			// TODO - Implement retry operation.
		}
		catch (DBConnectionRefusedException dbcrex) {
			// TODO - Implementation needed from other modules to decide what to do here.
		}
		catch (DBSQLException dbsqlex) {
			// TODO - Return MalformedSQLData msg to client.
		}
		catch (InvalidQueryParameterException iqpex) {
			// TODO - Return NoResultsData msg to client.
		}
		return ""; // This can be removed after TODOs are implemented.
	}

	public Boolean getIsOnSale(String goodID) {
		try {
			return TransactionValidityChecker.getIsOnSale(this.conn, goodID);
		}
		catch (DBClosedConnectionException dbccex) {
			// TODO - Implement retry operation.
		}
		catch (DBConnectionRefusedException dbcrex) {
			// TODO - Implementation needed from other modules to decide what to do here.
		}
		catch (DBSQLException dbsqlex) {
			// TODO - Return MalformedSQLData msg to client.
		}
		catch (InvalidQueryParameterException iqpex) {
			// TODO - Return NoResultsData msg to client.
		}
		return false; // This can be removed after TODOs are implemented.
	}
}
