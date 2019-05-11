package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidClientID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class GoodStateResponse extends BasicMessage implements Serializable {
    @NotNull(message = "The OwnerID cannot be null.")
    @NotEmpty(message = "The OwnerID cannot be empty.")
    @ValidClientID
    private String ownerID;

    @NotNull(message = "The onSale cannot be null.")
    private boolean onSale;

    // TODO - Add validators
    private String goodID;
    private String writerID;
    private long wts;
    private int rid;
    private String writeOperationSignature;

    public GoodStateResponse(long timestamp,
                             String requestID,
                             String operation,
                             String from,
                             String to,
                             String signature,
                             String ownerID,
                             boolean onSale,  // value associated with write
                             String goodID,   // goodId
                             String writerID, // entity that wrote the value goodId, onSale on the database
                             long wts,        // write time stamp
                             int rid,         // request identifier which has logical value only on the client side
                             String writeOperationSignature) // signature over onSale, goodId, writerId, wts)
    {
        super(timestamp, requestID, operation, from, to, signature);
        this.ownerID = ownerID;
        this.onSale = onSale;
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

    public String getWriterID() {
        return writerID;
    }

    public void setWriterID(String writerID) {
        this.writerID = writerID;
    }

    public long getWts() {
        return wts;
    }

    public void setWts(long wts) {
        this.wts = wts;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public String getWriteOperationSignature() {
        return writeOperationSignature;
    }

    public void setWriteOperationSignature(String writeOperationSignature) {
        this.writeOperationSignature = writeOperationSignature;
    }

    @Override
    public String toString() {
        return "GoodStateResponse{" +
                "ownerID='" + ownerID + '\'' +
                ", onSale=" + onSale +
                ", goodID='" + goodID + '\'' +
                ", writerID='" + writerID + '\'' +
                ", wts=" + wts +
                ", rid=" + rid +
                ", writeOperationSignature='" + writeOperationSignature + '\'' +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
