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

    @NotNull(message = "The write timestamp cannot be null.")
    private int writeTimestamp;

    @NotNull(message = "The on sale boolean cannot be null.")
    private Boolean onSale;

    @NotNull(message = "The write on good operation signature cannot be null.")
    @NotEmpty(message = "The write on good operation signature cannot be empty.")
    private String writeOnGoodsSignature;

    @NotNull(message = "The write on ownership operation signature cannot be null.")
    @NotEmpty(message = "The write on ownership operation signature cannot be empty.")
    private String writeOnOwnershipsSignature;

    public SaleRequestMessage(long timestamp, String requestID, String operation, String from, String to,
                              String signature, String goodID, String buyerID, String sellerID, int writeTimestamp,
                              Boolean onSale, String writeOnGoodsSignature, String writeOnOwnershipsSignature) {

        super(timestamp, requestID, operation, from, to, signature, goodID);
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.writeTimestamp = writeTimestamp;
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

    public int getWriteTimestamp() {
        return writeTimestamp;
    }

    public void setWriteTimestamp(int writeTimestamp) {
        this.writeTimestamp = writeTimestamp;
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
                ", logicalTimestamp=" + writeTimestamp +
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
