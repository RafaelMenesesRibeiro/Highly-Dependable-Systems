package hds.server.helpers;

import hds.server.ServerApplication;
import hds.server.exception.DBInitException;

import java.sql.*;

import static hds.server.ServerApplication.*;

@SuppressWarnings("Duplicates")
public class PSQLServerSetup {
	// TODO - Add to Application.properties just like datasource.url //
	private static final String URL = "jdbc:postgresql://localhost:5432/";


	public static void initDatabased() {
		if (Integer.parseInt(getPort()) == HDS_NOTARY_REPLICAS_FIRST_PORT) {
			int replicasNumber = getReplicasNumber();
			for (int i = 0; i < replicasNumber; i++) {
				initiSingleDB(DB_NAME_PREFIX + (HDS_NOTARY_REPLICAS_FIRST_PORT + i));
			}
		}
	}

	private static void initiSingleDB(String dbName) throws DBInitException {
		Connection connection = null;
		Connection connection2 = null;
		try {
			System.out.println("\n\n\n\n\n\n initSingleDB \n\n\n");
			Class.forName(ServerApplication.getDriver());
			connection = DriverManager.getConnection(URL, getUser(), getPassword());
			createDatabase(connection, dbName);
			String dbURL = URL + dbName;
			connection2 = DriverManager.getConnection(dbURL, getUser(), getPassword());
			createTables(connection2);
			populateTables(connection2);

		}
		catch (ClassNotFoundException | SQLException ex) {
			// TODO //
			System.out.println(ex.getMessage());
			System.out.println("\n\n\n\n\n\n EXCEPTION \n\n\n");
		}
		finally {
			try{
				if (connection != null) {
					connection.close();
				}
				if (connection2 != null) {
					connection2.close();
				}
			}
			catch(SQLException ex) { /* Nothing can be done. */ }
		}
	}

	private static void createDatabase(Connection connection, String dbName) throws DBInitException {
		System.out.println("\n\n\n\n\n\n createDatabase \n\n\n");
		String query = "DROP DATABASE IF EXISTS " + dbName + ";\n" + "CREATE DATABASE " + dbName + ";";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.execute();
		}
		catch (SQLException ex) {
			System.out.println("\n\n\n\n\n\n EXCEPTION \n\n\n");
			throw new DBInitException(ex.getMessage());
		}
	}

	// TODO - Read this from file. //
	private static void createTables(Connection connection) throws DBInitException {
		System.out.println("\n\n\n\n\n\n createTables \n\n\n");
		String query =
				"drop table if exists ownership cascade;" +
				"drop table if exists users cascade;" +
				"drop table if exists goods cascade;" +

				"CREATE TABLE users (" +
				"userId varchar(50), " +
				"CONSTRAINT pk_users PRIMARY KEY(userId));" +

				"CREATE TABLE goods (" +
				"goodId varchar(50), " +
				"onSale boolean, " +
				"CONSTRAINT pk_goods PRIMARY KEY (goodId));" +

				"CREATE TABLE ownership (" +
				"goodId varchar(50), " +
				"userId varchar(50), " +
				"CONSTRAINT pk_ownership PRIMARY KEY (goodId), " +
				"CONSTRAINT fk_ownership_goodId FOREIGN KEY (goodId) REFERENCES goods(goodId), " +
				"CONSTRAINT fk_ownership_userID FOREIGN KEY (userId) REFERENCES users(userId));" +

				"DROP TABLE IF EXISTS certificates;";

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.execute();
			System.out.println("Success create tables.");
		}
		catch (SQLException ex) {
			System.out.println("\n\n\n\n\n\n EXCEPTION \n\n\n");
			throw new DBInitException(ex.getMessage());
		}
	}

	// TODO - The number of users needs to match the actual number of users. //
	// TODO - Read this from file. //
	private static void populateTables(Connection connection) throws DBInitException {
		System.out.println("\n\n\n\n\n\n populateTables \n\n\n");
		String query =
				"delete from ownership;" +
				"delete from goods;" +
				"delete from users;" +

				"insert into users values ('8001');" +
				"insert into users values ('8002');" +
				"insert into users values ('8003');" +
				"insert into users values ('8004');" +

				"insert into goods values ('good1', false);" +
				"insert into goods values ('good2', false);" +
				"insert into goods values ('good3', true);" +
				"insert into goods values ('good4', true);" +

				"insert into ownership values ('good1', '8001');" +
				"insert into ownership values ('good2', '8002');" +
				"insert into ownership values ('good3', '8003');" +
				"insert into ownership values ('good4', '8004');";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.execute();
			System.out.println("Success populate tables.");
		}
		catch (SQLException ex) {
			System.out.println("\n\n\n\n\n\n EXCEPTION \n\n\n");
			throw new DBInitException(ex.getMessage());
		}
	}
}
