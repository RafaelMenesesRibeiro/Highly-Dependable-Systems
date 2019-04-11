package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidGoodID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class GoodDataMessage extends BasicMessage {
    @NotNull(message = "The GoodID cannot be null.")
    @NotEmpty(message = "The GoodID cannot be empty.")
    @ValidGoodID
    String goodID;

    public GoodDataMessage(String requestID, String operation, String from, String to, String signature, String goodID) {
        super(requestID, operation, from, to, signature);
        this.goodID = goodID;
    }

    public GoodDataMessage() {
    }

    public String getGoodID() {
        return goodID;
    }

    public void setGoodID(String goodID) {
        this.goodID = goodID;
    }

    @Override
    public String toString() {
        return "GoodDataMessage{" +
                "goodID='" + goodID + '\'' +
                ", requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
