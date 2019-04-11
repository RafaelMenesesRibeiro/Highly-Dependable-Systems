package hds.security.msgtypes;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class BasicMessage {
    long timestamp;

    @NotNull(message = "The requestID cannot be null.")
    @NotEmpty(message = "The requestID cannot be empty.")
    String requestID;

    @NotNull(message = "The operation cannot be null.")
    @NotEmpty(message = "The operation cannot be empty.")
    String operation;

    @NotNull(message = "The from cannot be null.")
    @NotEmpty(message = "The from cannot be empty.")
    String from;

    @NotNull(message = "The to cannot be null.")
    @NotEmpty(message = "The to cannot be empty.")
    String to;

    @NotNull(message = "The signature cannot be null.")
    @NotEmpty(message = "The signature cannot be empty.")
    String signature;

    public BasicMessage(long timestamp, String requestID, String operation, String from, String to, String signature) {
        this.timestamp = timestamp;
        this.requestID = requestID;
        this.operation = operation;
        this.from = from;
        this.to = to;
        this.signature = signature;
    }

    public BasicMessage() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "BasicMessage{" +
                "requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}


