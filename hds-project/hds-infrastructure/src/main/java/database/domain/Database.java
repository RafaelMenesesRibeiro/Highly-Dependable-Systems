package database.domain;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Database {
	private Connection conn;
	private String endpoint;
	private String name;
	private String username;
	private String password;

	public Database() {
		FetchProperties("aws-rds-db.properties");
		conn = connecToDB();

		// TODO - Find out why postgres doesn't create tables with this. //
		//String creationQuery = GetQuery("schemas.sql");
		//QueryDB(conn, creationQuery);
		// TODO - Move to own function and only call once in Main or in PuppetMaster Main. //
		//String populateQuery = GetQuery("populate.sql");
		//QueryDB(conn, populateQuery, "userId");

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
		catch (SQLException e) { System.out.println(e.getMessage()); }
		return conn;
	}

	public List<String> QueryDB(Connection conn, String query, String returnColumn) {
		List<String> results = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				results.add(rs.getString(returnColumn));
				System.out.println(String.format("RESULT: %s", rs.getString(returnColumn)));
			}
			stmt.close();
			rs.close();
		}
		catch (SQLException ex) { System.out.println(ex.getMessage()); }
		return results;
	}

	private String GetQuery(String filename) {
		try {
			InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName(StandardCharsets.UTF_8.name())));
			String data = "";
			String line;
			while ((line = reader.readLine()) != null) {
				data += line;
			}
			return data;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String getCurrentOwner(String goodID) {
		String query = "select (ownership.userId) from ownership where ownership.goodId = '" + goodID + "'";
		System.out.println(query);
		List<String> results = QueryDB(this.conn, query, "userId");
		return results.get(0);
	}

	public Boolean getIsOnSale(String goodID) {
		String query = "select (goods.onSale) from goods where goods.goodId = " + goodID;
		System.out.println(query);
		List<String> results = QueryDB(this.conn, query, "onSale");
		return results.get(0) == "true";
	}
}
