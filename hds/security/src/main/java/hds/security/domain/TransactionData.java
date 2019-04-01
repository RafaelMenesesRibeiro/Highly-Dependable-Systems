package com.security.domain;

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
        return this.buyerID;
    }

    public String getGoodID() {
        return this.goodID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public void setBuyerID(String buyerID) {
        this.buyerID = buyerID;
    }

    public void setGoodID(String goodID) {
        this.goodID = goodID;
    }
}
