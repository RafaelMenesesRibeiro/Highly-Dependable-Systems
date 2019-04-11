package hds.security.msgtypes;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BasicMessage {
    String requestID;

    @NotNull(message = "TESTING IF THIS WORKS .....")
    @Size(min = 2, message = "TESTING 2 IF THIS WORKS 2. ....")
    String operation;
    String from;
    String to;
    String signature;

    public BasicMessage(String requestID, String operation, String from, String to, String signature) {
        this.requestID = requestID;
        this.operation = operation;
        this.from = from;
        this.to = to;
        this.signature = signature;
    }

    public BasicMessage() {
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


