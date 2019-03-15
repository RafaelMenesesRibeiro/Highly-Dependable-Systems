import java.sql.Connection;

import client.domain.Client;
import database.domain.Database;
import server.domain.Server;

public class Application {
    public static void main(String[] args) {
		System.out.println("Puppet Master's Main");
    	Server server = new Server("localhost", "8014");
    	Client client = new Client("client1");
    	Database db = new Database();
    }
}