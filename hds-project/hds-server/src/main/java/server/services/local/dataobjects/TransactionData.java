package server.services.local.dataobjects;

public class TransactionData {
	private String sellerID;
	private String buyerID;
	private String goodID;

	public TransactionData(String sellerID, String buyerID, String goodID) {
		this.sellerID = sellerID;
		this.buyerID = buyerID;
		this.goodID = goodID;
	}

	public String getSellerID() {
		return this.sellerID;
	}

	public String getBuyerID() {
		return this.sellerID;
	}

	public String getGoodID() {
		return this.goodID;
	}
}
