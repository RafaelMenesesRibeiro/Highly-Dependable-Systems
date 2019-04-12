package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidClientID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class SaleRequestMessage extends  GoodDataMessage implements Serializable {
    @NotNull(message = "The BuyerID cannot be null.")
    @NotEmpty(message = "The BuyerID cannot be empty.")
    @ValidClientID
    private String buyerID;

    @NotNull(message = "The SellerID cannot be null.")
    @NotEmpty(message = "The SellerID cannot be empty.")
    @ValidClientID
    private String sellerID;

    public SaleRequestMessage(long timestamp, String requestID, String operation, String from, String to,
                              String signature, String goodID, String buyerID, String sellerID) {
        super(timestamp, requestID, operation, from, to, signature, goodID);
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
