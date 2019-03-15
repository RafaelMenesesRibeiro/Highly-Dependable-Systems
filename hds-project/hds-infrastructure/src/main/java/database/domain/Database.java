package database.domain;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import database.exception.*;

public class Database {
	private Connection conn;
	private String endpoint;
	private String name;
	private String username;
	private String password;

	public Database() {
		FetchProperties("aws-rds-db.properties");
		this.conn = connecToDB();
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
				throw new IOException("Unable to properties file. Can't access database.\n " +
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

	private Connection connecToDB() {
		String url = String.format("jdbc:postgresql:%s/%s", this.endpoint, this.name);
		try {
			return DriverManager.getConnection(url, this.username, this.password);
		}
		catch (SQLException sqlex) {
			System.out.println(sqlex.getMessage());
			System.exit(1);
		}
		return null;
	}

	private List<String> QueryDB(Connection conn, String query, String returnColumn)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException {
		List<String> results = new ArrayList<String>();
		try {
			// TODO - Create pre prepared statements. //
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				results.add(rs.getString(returnColumn));
				System.out.println(String.format("RESULT: %s", rs.getString(returnColumn)));
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
			switch (String.valueOf((sqlex.getErrorCode()))) {
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
					throw new DBSQLException(sqlex.getMessage());
				default:
					System.out.println(sqlex.getMessage());
					System.exit(1);
			}
		}
		return results;
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

	public String getCurrentOwner(String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException {
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'getCurrentOwner' is either null or an empty string.");
		}
		String query = "select (ownership.userId) from ownership where ownership.goodId = '" + goodID + "'";
		try {
			List<String> results = QueryDB(this.conn, query, "userId");
			return results.get(0);
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException are ignored to be
		// caught up the chain.
		catch (IndexOutOfBoundsException ioobex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}

	public Boolean getIsOnSale(String goodID)
			throws DBClosedConnectionException, DBConnectionRefusedException, DBSQLException, InvalidQueryParameterException {
		if (goodID == null || goodID.equals("")) {
			throw new InvalidQueryParameterException("The parameter 'goodID' in query 'getIsOnSale' is either null or an empty string.");
		}
		String query = "select (goods.onSale) from goods where goods.goodId = " + goodID;
		try {
			List<String> results = QueryDB(this.conn, query, "onSale");
			return results.get(0).equals("true");
		}
		// DBClosedConnectionException | DBConnectionRefusedException | DBSQLException are ignored to be
		// caught up the chain.
		catch (IndexOutOfBoundsException ioobex) {
			throw new DBNoResultsException("The query \"" + query + "\" returned no results.");
		}
	}
}
