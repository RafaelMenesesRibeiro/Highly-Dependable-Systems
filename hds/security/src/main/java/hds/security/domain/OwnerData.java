package hds.security.domain;

import java.io.Serializable;

public class OwnerData implements Serializable {
	private String sellerID;
	private String goodID;

	public OwnerData() {}

	public OwnerData(String sellerID, String goodID) {
		super();
		this.sellerID = sellerID;
		this.goodID = goodID;
	}

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
