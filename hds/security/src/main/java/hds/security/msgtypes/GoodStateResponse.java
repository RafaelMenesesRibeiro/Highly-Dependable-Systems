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
    private long onGoodsWriteTimestamp;

    @NotNull(message = "The write on goods operation signature cannot be null.")
    @NotEmpty(message = "The write on goods operation signature cannot be empty.")
    private String writeOnGoodsSignature;

    @NotNull(message = "The on ownership WriterID cannot be null.")
    @NotEmpty(message = "The on ownership WriterID cannot be empty.")
    @ValidGoodID
    private String onOwnershipWriterID;

    @NotNull(message = "The on ownership WriteTimestamp cannot be null.")
    @NotFutureTimestamp
    private long onOwnershipWriteTimestamp;

    @NotNull(message = "The write on ownership operation signature cannot be null.")
    @NotEmpty(message = "The write on ownership operation signature cannot be empty.")
    private String writeOnOwnershipSignature;

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
                             long onGoodsWriteTimestamp,
                             String writeOnGoodsSignature,
                             long onOwnershipWriteTimestamp,
                             String writeOnOwnershipSignature,
                             int rid
                             )
    {
        super(timestamp, requestID, operation, from, to, signature);
        this.goodID = goodID;
        this.ownerID = ownerID;
        this.onSale = onSale;
        this.onGoodsWriterID = onGoodsWriterID;
        this.onGoodsWriteTimestamp = onGoodsWriteTimestamp;
        this.writeOnGoodsSignature = writeOnGoodsSignature;
        this.onOwnershipWriterID = ownerID;
        this.onOwnershipWriteTimestamp = onOwnershipWriteTimestamp;
        this.writeOnOwnershipSignature = writeOnOwnershipSignature;
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

    public long getOnGoodsWriteTimestamp() {
        return onGoodsWriteTimestamp;
    }

    public void setOnGoodsWriteTimestamp(long onGoodsWriteTimestamp) {
        this.onGoodsWriteTimestamp = onGoodsWriteTimestamp;
    }

    public String getWriteOnGoodsSignature() {
        return writeOnGoodsSignature;
    }

    public void setWriteOnGoodsSignature(String writeOnGoodsSignature) {
        this.writeOnGoodsSignature = writeOnGoodsSignature;
    }

    public String getOnOwnershipWriterID() {
        return onOwnershipWriterID;
    }

    public void setOnOwnershipWriterID(String onOwnershipWriterID) {
        this.onOwnershipWriterID = onOwnershipWriterID;
    }

    public long getOnOwnershipWriteTimestamp() {
        return onOwnershipWriteTimestamp;
    }

    public void setOnOwnershipWriteTimestamp(long onOwnershipWriteTimestamp) {
        this.onOwnershipWriteTimestamp = onOwnershipWriteTimestamp;
    }

    public String getWriteOnOwnershipSignature() {
        return writeOnOwnershipSignature;
    }

    public void setWriteOnOwnershipSignature(String writeOnOwnershipSignature) {
        this.writeOnOwnershipSignature = writeOnOwnershipSignature;
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
                ", onGoodsWts=" + onGoodsWriteTimestamp +
                ", writeOnGoodsOperationSignature='" + writeOnGoodsSignature + '\'' +
                ", onOwnershipWriterID='" + onOwnershipWriterID + '\'' +
                ", onOwnershipWts=" + onOwnershipWriteTimestamp +
                ", writeOnOwnershipOperationSignature='" + writeOnOwnershipSignature + '\'' +
                ", rid=" + rid +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
