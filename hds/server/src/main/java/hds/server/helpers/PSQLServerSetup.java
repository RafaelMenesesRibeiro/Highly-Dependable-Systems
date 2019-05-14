package hds.server.helpers;

import hds.server.ServerApplication;
import hds.server.exception.DBInitException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hds.server.ServerApplication.*;


/**
 * Creates and populates a Database for each created server.
 *
 * @author 		Rafael Ribeiro
 */
public class PSQLServerSetup {

	/**
	 * Initializes the databases for every server.
	 */
	public static void initDatabased() {
		if (Integer.parseInt(getPort()) == HDS_NOTARY_REPLICAS_FIRST_PORT) {
			int regularReplicasNumber = getRegularReplicasNumber();
			for (int i = 0; i < regularReplicasNumber; i++) {
				initiSingleDB(DB_NAME_PREFIX + (HDS_NOTARY_REPLICAS_FIRST_PORT + i));
			}

			int ccReplicasNumber = getCCReplicasNumber();
			for (int i = 0; i < ccReplicasNumber; i++) {
				initiSingleDB(DB_NAME_PREFIX + (HDS_NOTARY_REPLICAS_FIRST_CC_PORT + i));
			}
		}
	}

	/**
	 * Initializes a Database with a given name.
	 *
	 * @param 	dbName			Name of the database to be created
	 * @throws 	DBInitException	The creation / population of the database was not successful
	 */
	private static void initiSingleDB(String dbName) throws DBInitException {
		Connection connection = null;
		Connection connection2 = null;
		try {
			Class.forName(ServerApplication.getDriver());
			connection = DriverManager.getConnection(getDatabaseServerUrl(), getUser(), getPassword());
			createDatabase(connection, dbName);
			String dbURL = getDatabaseServerUrl() + dbName;
			connection2 = DriverManager.getConnection(dbURL, getUser(), getPassword());
			createTables(connection2);
			populateTables(connection2);

		}
		catch (ClassNotFoundException | SQLException ex) {
			throw new DBInitException(ex.getMessage());
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

	/**
	 * Creates the database
	 *
	 * @param 	connection 		Connection to the database's server
	 * @param 	dbName			Name of the database to be created
	 * @throws 	DBInitException	The creation / population of the database was not successful
	 */
	private static void createDatabase(Connection connection, String dbName) throws DBInitException {
		String query = "DROP DATABASE IF EXISTS " + dbName + ";\n" + "CREATE DATABASE " + dbName + ";";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.execute();
		}
		catch (SQLException ex) {
			throw new DBInitException(ex.getMessage());
		}
	}

	/**
	 * Creates the tables
	 *
	 * @param 	connection 		Connection to the database
	 * @throws 	DBInitException	The creation / population of the database was not successful
	 */
	private static void createTables(Connection connection) throws DBInitException {
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
			"wid varchar(50), " +
			"ts text, " +
			"sig text, " +
			"CONSTRAINT pk_goods PRIMARY KEY (goodId));" +

			"CREATE TABLE ownership (" +
			"goodId varchar(50), " +
			"userId varchar(50), " +
			"ts text, " +
			"sig text, " +
			"CONSTRAINT pk_ownership PRIMARY KEY (goodId), " +
			"CONSTRAINT fk_ownership_goodId FOREIGN KEY (goodId) REFERENCES goods(goodId), " +
			"CONSTRAINT fk_ownership_userID FOREIGN KEY (userId) REFERENCES users(userId));" +

			"drop table if exists certificates;";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.execute();
		}
		catch (SQLException ex) {
			throw new DBInitException(ex.getMessage());
		}
	}

	/**
	 * Populates the tables
	 *
	 * @param 	connection 		Connection to the database
	 * @throws 	DBInitException	The creation / population of the database was not successful
	 */
	private static void populateTables(Connection connection) throws DBInitException {
		String query =
			"delete from ownership;" +
			"delete from goods;" +
			"delete from users;" +

			"insert into users values ('8001');" +
			"insert into users values ('8002');" +
			"insert into users values ('8003');" +
			"insert into users values ('8004');" +

			"insert into goods values ('good1', false, '8001', '0', 'initialSign');" +
			"insert into goods values ('good2', false, '8002', '0', 'initialSign');" +
			"insert into goods values ('good3', true, '8003', '0', 'initialSign');" +
			"insert into goods values ('good4', true, '8004', '0', 'initialSign');" +

			"insert into ownership values ('good1', '8001', '0', 'initialSign');" +
			"insert into ownership values ('good2', '8002', '0', 'initialSign');" +
			"insert into ownership values ('good3', '8003', '0', 'initialSign');" +
			"insert into ownership values ('good4', '8004', '0', 'initialSign');";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.execute();
		}
		catch (SQLException ex) {
			throw new DBInitException(ex.getMessage());
		}
	}
}
