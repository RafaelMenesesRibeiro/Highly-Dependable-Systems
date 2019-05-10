package hds.security.msgtypes;

import com.sun.org.apache.xpath.internal.operations.Bool;
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

    private int logicalTimestamp;
    private Boolean onSale;
    private String writeOnGoodsSignature;
    private String writeOnOwnershipsSignature;

    public SaleRequestMessage(long timestamp, String requestID, String operation, String from, String to,
                              String signature, String goodID, String buyerID, String sellerID, int logicalTimestamp,
                              Boolean onSale, String writeOnGoodsSignature, String writeOnOwnershipsSignature) {

        super(timestamp, requestID, operation, from, to, signature, goodID);
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.logicalTimestamp = logicalTimestamp;
        this.onSale = onSale;
        this.writeOnGoodsSignature = writeOnGoodsSignature;
        this.writeOnOwnershipsSignature = writeOnOwnershipsSignature;
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

    public int getLogicalTimestamp() {
        return logicalTimestamp;
    }

    public void setLogicalTimestamp(int logicalTimestamp) {
        this.logicalTimestamp = logicalTimestamp;
    }

    public Boolean getOnSale() {
        return onSale;
    }

    public void setOnSale(Boolean onSale) {
        this.onSale = onSale;
    }

    public String getWriteOnGoodsSignature() {
        return writeOnGoodsSignature;
    }

    public void setWriteOnGoodsSignature(String writeOnGoodsSignature) {
        this.writeOnGoodsSignature = writeOnGoodsSignature;
    }

    public String getwriteOnOwnershipsSignature() {
        return writeOnOwnershipsSignature;
    }

    public void setwriteOnOwnershipsSignature(String writeOnOwnershipsSignature) {
        this.writeOnOwnershipsSignature = writeOnOwnershipsSignature;
    }

    @Override
    public String toString() {
        return "SaleRequestMessage{" +
                "buyerID='" + buyerID + '\'' +
                ", sellerID='" + sellerID + '\'' +
                ", logicalTimestamp=" + logicalTimestamp +
                ", onSale=" + onSale +
                ", writeOnGoodsSignature='" + writeOnGoodsSignature + '\'' +
                ", writeOnOwnershipsSignature='" + writeOnOwnershipsSignature + '\'' +
                ", goodID='" + goodID + '\'' +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
