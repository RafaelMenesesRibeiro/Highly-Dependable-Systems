package server.domain;

import database.domain.Database;
import server.services.local.dataobjects.TransactionData;

public class Server {
	private Database database;
	private String ip;
	private String port;

	public Server(String ip, String port, Database database) {
		this.ip = ip;
		this.port = port;
		this.database = database;
		System.out.println(String.format("Created server with ip=%s and port=%s", ip, port));
	}

	// TODO - Change to private after initial testing. //
	public boolean IsTransactionValid(TransactionData transaction) {
		String sellerID = transaction.getSellerID();
		String goodID = transaction.getGoodID();
		return IsSellerOwner(sellerID, goodID) && IsGoodOnSale(goodID);
	}

	private boolean IsSellerOwner(String sellerID, String goodID) {
		String currentOwner = database.getCurrentOwner(goodID);
		return currentOwner.equals(sellerID);
	}

	private boolean IsGoodOnSale(String goodID) {
		return database.getIsOnSale(goodID);
	}
}