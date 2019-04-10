package hds.security.msgtypes;

public class OwnerDataMessage extends GoodDataMessage {
    private String owner;

    public OwnerDataMessage(int requestID, String operation, String from, String to, String signature, String goodID, String owner) {
        super(requestID, operation, from, to, signature, goodID);
        this.owner = owner;
    }

    public OwnerDataMessage(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "OwnerDataMessage{" +
                "owner='" + owner + '\'' +
                ", goodID='" + goodID + '\'' +
                ", requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
