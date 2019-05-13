package hds.security.msgtypes;

import hds.security.helpers.inputValidation.NotFutureTimestamp;
import hds.security.helpers.inputValidation.ValidClientID;
import hds.security.helpers.inputValidation.ValidGoodID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class GoodStateResponse extends BasicMessage implements Serializable {
    @NotNull(message = "The GoodID cannot be null.")
    @NotEmpty(message = "The GoodID cannot be empty.")
    @ValidGoodID
    private String goodID;

    @NotNull(message = "The OwnerID cannot be null.")
    @NotEmpty(message = "The OwnerID cannot be empty.")
    @ValidClientID
    private String ownerID;

    @NotNull(message = "The onSale cannot be null.")
    private boolean onSale;

    @NotNull(message = "The on goods WriterID cannot be null.")
    @NotEmpty(message = "The on goods WriterID cannot be empty.")
    @ValidGoodID
    private String onGoodsWriterID;

    @NotNull(message = "The on goods WriteTimestamp cannot be null.")
    @NotFutureTimestamp
    private long onGoodsWts;

    @NotNull(message = "The write on goods operation signature cannot be null.")
    @NotEmpty(message = "The write on goods operation signature cannot be empty.")
    private String writeOnGoodsOperationSignature;

    @NotNull(message = "The on ownership WriterID cannot be null.")
    @NotEmpty(message = "The on ownership WriterID cannot be empty.")
    @ValidGoodID
    private String onOwnershipWriterID;

    @NotNull(message = "The on ownership WriteTimestamp cannot be null.")
    @NotFutureTimestamp
    private long onOwnershipWts;

    @NotNull(message = "The write on ownership operation signature cannot be null.")
    @NotEmpty(message = "The write on ownership operation signature cannot be empty.")
    private String writeOnOwnershipOperationSignature;

    @NotNull(message = "The readID cannot be null.")
    private int rid;

    public GoodStateResponse(long timestamp,
                             String requestID,
                             String operation,
                             String from,
                             String to,
                             String signature,
                             String goodID,
                             String ownerID,  // value associated with write
                             boolean onSale,   // goodId
                             String onGoodsWriterID,
                             long onGoodsWts,
                             String writeOnGoodsOperationSignature,
                             long onOwnershipWts,
                             String writeOnOwnershipOperationSignature,
                             int rid
                             )
    {
        super(timestamp, requestID, operation, from, to, signature);
        this.goodID = goodID;
        this.ownerID = ownerID;
        this.onSale = onSale;
        this.onGoodsWriterID = onGoodsWriterID;
        this.onGoodsWts = onGoodsWts;
        this.writeOnGoodsOperationSignature = writeOnGoodsOperationSignature;
        this.onOwnershipWriterID = ownerID;
        this.onOwnershipWts = onOwnershipWts;
        this.writeOnOwnershipOperationSignature = writeOnOwnershipOperationSignature;
        this.rid = rid;
    }

    public GoodStateResponse() {
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public void setOnSale(boolean onSale) {
        this.onSale = onSale;
    }

    public String getGoodID() {
        return goodID;
    }

    public void setGoodID(String goodID) {
        this.goodID = goodID;
    }

    public String getOnGoodsWriterID() {
        return onGoodsWriterID;
    }

    public void setOnGoodsWriterID(String onGoodsWriterID) {
        this.onGoodsWriterID = onGoodsWriterID;
    }

    public long getOnGoodsWts() {
        return onGoodsWts;
    }

    public void setOnGoodsWts(long onGoodsWts) {
        this.onGoodsWts = onGoodsWts;
    }

    public String getWriteOnGoodsOperationSignature() {
        return writeOnGoodsOperationSignature;
    }

    public void setWriteOnGoodsOperationSignature(String writeOnGoodsOperationSignature) {
        this.writeOnGoodsOperationSignature = writeOnGoodsOperationSignature;
    }

    public String getOnOwnershipWriterID() {
        return onOwnershipWriterID;
    }

    public void setOnOwnershipWriterID(String onOwnershipWriterID) {
        this.onOwnershipWriterID = onOwnershipWriterID;
    }

    public long getOnOwnershipWts() {
        return onOwnershipWts;
    }

    public void setOnOwnershipWts(long onOwnershipWts) {
        this.onOwnershipWts = onOwnershipWts;
    }

    public String getWriteOnOwnershipOperationSignature() {
        return writeOnOwnershipOperationSignature;
    }

    public void setWriteOnOwnershipOperationSignature(String writeOnOwnershipOperationSignature) {
        this.writeOnOwnershipOperationSignature = writeOnOwnershipOperationSignature;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    @Override
    public String toString() {
        return "GoodStateResponse{" +
                "goodID='" + goodID + '\'' +
                ", ownerID='" + ownerID + '\'' +
                ", onSale=" + onSale +
                ", onGoodsWriterID='" + onGoodsWriterID + '\'' +
                ", onGoodsWts=" + onGoodsWts +
                ", writeOnGoodsOperationSignature='" + writeOnGoodsOperationSignature + '\'' +
                ", onOwnershipWriterID='" + onOwnershipWriterID + '\'' +
                ", onOwnershipWts=" + onOwnershipWts +
                ", writeOnOwnershipOperationSignature='" + writeOnOwnershipOperationSignature + '\'' +
                ", rid=" + rid +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
