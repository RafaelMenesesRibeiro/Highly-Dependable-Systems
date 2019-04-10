package hds.security.msgtypes;

public class ApproveSaleRequestMessage extends SaleRequestMessage {
    String wrappingOperation;
    String wrappingFrom;
    String wrappingTo;
    String wrappingSignature;

    public ApproveSaleRequestMessage(int requestID, String operation, String from, String to, String signature, String goodID, String buyerID, String sellerID, String wrappingOperation, String wrappingFrom, String wrappingTo, String wrappingSignature) {
        super(requestID, operation, from, to, signature, goodID, buyerID, sellerID);
        this.wrappingOperation = wrappingOperation;
        this.wrappingFrom = wrappingFrom;
        this.wrappingTo = wrappingTo;
        this.wrappingSignature = wrappingSignature;
    }

    public ApproveSaleRequestMessage() {
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
