package hds.security.msgtypes;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ApproveSaleRequestMessage extends SaleRequestMessage implements Serializable {
    @NotNull(message = "The wrappingTimestamp cannot be null.")
    private long wrappingTimestamp;

    @NotNull(message = "The wrappingOperation cannot be null.")
    @NotEmpty(message = "The wrappingOperation cannot be empty.")
    private String wrappingOperation;

    @NotNull(message = "The wrappingFrom cannot be null.")
    @NotEmpty(message = "The wrappingFrom cannot be empty.")
    private String wrappingFrom;

    @NotNull(message = "The wrappingTo cannot be null.")
    @NotEmpty(message = "The wrappingTo cannot be empty.")
    private String wrappingTo;

    @NotNull(message = "The wrappingSignature cannot be null.")
    @NotEmpty(message = "The wrappingSignature cannot be empty.")
    private String wrappingSignature;

    public ApproveSaleRequestMessage(long timesamp, String requestID, String operation, String from, String to,
                                     String signature,
                                     String goodID, String buyerID, String sellerID,
                                     long wrappingTimestamp, String wrappingOperation, String wrappingFrom,
                                     String wrappingTo,
                                     String wrappingSignature) {
        super(timesamp, requestID, operation, from, to, signature, goodID, buyerID, sellerID);
        this.wrappingTimestamp = wrappingTimestamp;
        this.wrappingOperation = wrappingOperation;
        this.wrappingFrom = wrappingFrom;
        this.wrappingTo = wrappingTo;
        this.wrappingSignature = wrappingSignature;
    }

    public ApproveSaleRequestMessage() {
    }

    public long getWrappingTimestamp() {
        return wrappingTimestamp;
    }

    public String getWrappingOperation() {
        return wrappingOperation;
    }

    public void setWrappingOperation(String wrappingOperation) {
        this.wrappingOperation = wrappingOperation;
    }

    public String getWrappingFrom() {
        return wrappingFrom;
    }

    public void setWrappingFrom(String wrappingFrom) {
        this.wrappingFrom = wrappingFrom;
    }

    public String getWrappingTo() {
        return wrappingTo;
    }

    public void setWrappingTo(String wrappingTo) {
        this.wrappingTo = wrappingTo;
    }

    public String getWrappingSignature() {
        return wrappingSignature;
    }

    public void setWrappingSignature(String wrappingSignature) {
        this.wrappingSignature = wrappingSignature;
    }

    @Override
    public String toString() {
        return "ApproveSaleRequestMessage{" +
                "wrappingOperation='" + wrappingOperation + '\'' +
                ", wrappingFrom='" + wrappingFrom + '\'' +
                ", wrappingTo='" + wrappingTo + '\'' +
                ", wrappingSignature='" + wrappingSignature + '\'' +
                ", goodID='" + goodID + '\'' +
                ", requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
