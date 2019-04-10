package hds.security.msgtypes;

public class GoodStateResponse extends BasicMessage {
    private String ownerID;
    private boolean onSale;

    public GoodStateResponse(String requestID, String operation, String from, String to, String signature, String ownerID, boolean onSale) {
        super(requestID, operation, from, to, signature);
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

    @Override
    public String toString() {
        return "GoodStateResponse{" +
                "ownerID='" + ownerID + '\'' +
                ", onSale=" + onSale +
                ", requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
