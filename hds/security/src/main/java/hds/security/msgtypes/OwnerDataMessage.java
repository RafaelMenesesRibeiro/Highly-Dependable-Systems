package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidClientID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class OwnerDataMessage extends GoodDataMessage {
    @NotNull(message = "The ownerID cannot be null.")
    @NotEmpty(message = "The ownerID cannot be empty.")
    @ValidClientID
    private String owner;

    public OwnerDataMessage(long timestamp, String requestID, String operation, String from, String to, String signature,
                            String goodID, String owner) {
        super(timestamp, requestID, operation, from, to, signature, goodID);
        this.owner = owner;
    }

    public OwnerDataMessage() {}


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
