package hds.security.domain;

public class OwnerData {
	private String sellerID;
	private String goodID;

	public OwnerData(String sellerID, String goodID) {
		super();
		this.sellerID = sellerID;
		this.goodID = goodID;
	}

	public OwnerData() {}

	public String getSellerID() {
		return sellerID;
	}

	public void setSellerID(String sellerID) {
		this.sellerID = sellerID;
	}

	public String getGoodID() {
		return goodID;
	}

	public void setGoodID(String goodID) {
		this.goodID = goodID;
	}
}
