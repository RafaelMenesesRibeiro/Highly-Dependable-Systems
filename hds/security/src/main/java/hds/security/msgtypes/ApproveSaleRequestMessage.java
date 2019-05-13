package hds.security.msgtypes;

import hds.security.helpers.inputValidation.NotFutureTimestamp;
import hds.security.helpers.inputValidation.RelevantTimestamp;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ApproveSaleRequestMessage extends SaleRequestMessage implements Serializable {
    @NotNull(message = "The wrappingTimestamp cannot be null.")
    @RelevantTimestamp
    @NotFutureTimestamp
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

    @NotNull(message = "The challengeResponse cannot be null.")
    @NotEmpty(message = "The challengeResponse cannot be empty.")
    private String challengeResponse;

    public ApproveSaleRequestMessage(long timestamp,
                                     String requestID,
                                     String operation,
                                     String from,
                                     String to,
                                     String signature,
                                     String goodID,
                                     String buyerID,
                                     String sellerID,
                                     long wts,
                                     Boolean onSale,
                                     String writeOnGoodsSignature,
                                     String writeOnOwnershipsSignature,
                                     long wrappingTimestamp,
                                     String wrappingOperation,
                                     String wrappingFrom,
                                     String wrappingTo,
                                     String wrappingSignature,
                                     String challengeResponse) {

        super(timestamp, requestID, operation, from, to, signature, goodID, buyerID, sellerID, wts, onSale, writeOnGoodsSignature, writeOnOwnershipsSignature);
        this.wrappingTimestamp = wrappingTimestamp;
        this.wrappingOperation = wrappingOperation;
        this.wrappingFrom = wrappingFrom;
        this.wrappingTo = wrappingTo;
        this.wrappingSignature = wrappingSignature;
        this.challengeResponse = challengeResponse;
    }

    public ApproveSaleRequestMessage() {

    }

    public long getWrappingTimestamp() {
        return wrappingTimestamp;
    }

    public void setWrappingTimestamp(long wrappingTimestamp) {
        this.wrappingTimestamp = wrappingTimestamp;
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

    public String getChallengeResponse() {
        return challengeResponse;
    }

    public void setChallengeResponse(String challengeResponse) {
        this.challengeResponse = challengeResponse;
    }

    @Override
    public String toString() {
        return "ApproveSaleRequestMessage{" +
                "wrappingTimestamp=" + wrappingTimestamp +
                ", wrappingOperation='" + wrappingOperation + '\'' +
                ", wrappingFrom='" + wrappingFrom + '\'' +
                ", wrappingTo='" + wrappingTo + '\'' +
                ", wrappingSignature='" + wrappingSignature + '\'' +
                ", challengeResponse='" + challengeResponse + '\'' +
                ", goodID='" + goodID + '\'' +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
