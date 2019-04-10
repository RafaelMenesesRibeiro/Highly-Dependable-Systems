package hds.security.msgtypes;

public class SaleRequestMessage extends  GoodDataMessage {
    private String buyerID;
    private String sellerID;

    public SaleRequestMessage(int requestID, String operation, String from, String to, String signature, String goodID, String buyerID, String sellerID) {
        super(requestID, operation, from, to, signature, goodID);
        this.buyerID = buyerID;
        this.sellerID = sellerID;
    }

    public SaleRequestMessage() {
    }

    public String getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(String buyerID) {
        this.buyerID = buyerID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    @Override
    public String toString() {
        return "SaleRequestMessage{" +
                "buyerID='" + buyerID + '\'' +
                ", sellerID='" + sellerID + '\'' +
                ", goodID='" + goodID + '\'' +
                ", requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
