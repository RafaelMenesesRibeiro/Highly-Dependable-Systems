package com.security.domain;

public class OwnerData {
	private final String sellerID;
	private final String goodID;

	public OwnerData(String sellerID, String goodID) {
		super();
		this.sellerID = sellerID;
		this.goodID = goodID;
	}

	public String getSellerID() {
		return sellerID;
	}

	public String getGoodID() {
		return goodID;
	}
}
