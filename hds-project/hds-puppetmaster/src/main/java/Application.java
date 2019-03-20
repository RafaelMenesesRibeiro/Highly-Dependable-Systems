import java.sql.Connection;

import client.domain.Client;
import database.domain.Database;
import server.domain.Server;
import server.services.local.dataobjects.TransactionData;

public class Application {
    public static void main(String[] args) {
		System.out.println("Puppet Master's Main");
		Database db = new Database();
		// db.CreateTables();
		// db.PopulateTables();

		Server server = new Server("localhost", "8014", db);
    	Client client = new Client("client1");

		TransactionData transaction = new TransactionData("user3", "user2", "good3");
		boolean res = server.IsTransactionValid(transaction);
		System.out.println("The transaction validity was: " + res);
    }
}