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

    public GoodStateResponse(long timestamp, String requestID, String operation, String from, String to,
                             String signature, String ownerID, boolean onSale) {
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
