package hds.security.domain;

public class TransactionData {
    private String sellerID;
    private String buyerID;
    private String goodID;

    public TransactionData() {}

    public TransactionData(String sellerID, String buyerID, String goodID) {
        this.sellerID = sellerID;
        this.buyerID = buyerID;
        this.goodID = goodID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public String getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(String buyerID) {
        this.buyerID = buyerID;
    }

    public String getGoodID() {
        return goodID;
    }

    public void setGoodID(String goodID) {
        this.goodID = goodID;
    }
}
