package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidClientID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class OwnerDataMessage extends GoodDataMessage implements Serializable {
    @NotNull(message = "The logical timestamp cannot be null.")
    private long wts;

    @NotNull(message = "The on sale boolean cannot be null.")
    private boolean onSale;

    @NotNull(message = "The ownerID cannot be null.")
    @NotEmpty(message = "The ownerID cannot be empty.")
    @ValidClientID
    private String owner;

    @NotNull(message = "The write operation signature cannot be null.")
    @NotEmpty(message = "The write operation signature cannot be empty.")
    private String writeOperationSignature;

    public OwnerDataMessage(long timestamp,
                            String requestID,
                            String operation,
                            String from,
                            String to,
                            String signature,
                            String goodID,
                            String owner,
                            long wts,
                            boolean onSale,
                            String writeOperationSignature) {

        super(timestamp, requestID, operation, from, to, signature, goodID);
        this.owner = owner;
        this.wts = wts;
        this.onSale = onSale;
        this.writeOperationSignature = writeOperationSignature;
    }

    public OwnerDataMessage() {}

    public long getWts() {
        return wts;
    }

    public void setWts(long wts) {
        this.wts = wts;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public void setOnSale(boolean onSale) {
        this.onSale = onSale;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getWriteOperationSignature() {
        return writeOperationSignature;
    }

    public void setWriteOperationSignature(String writeOperationSignature) {
        this.writeOperationSignature = writeOperationSignature;
    }

    @Override
    public String toString() {
        return "OwnerDataMessage{" +
                "wts=" + wts +
                ", onSale=" + onSale +
                ", owner='" + owner + '\'' +
                ", writeOperationSignature='" + writeOperationSignature + '\'' +
                ", goodID='" + goodID + '\'' +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
