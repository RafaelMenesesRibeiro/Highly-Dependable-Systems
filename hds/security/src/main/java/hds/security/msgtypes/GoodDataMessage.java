package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidGoodID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class GoodDataMessage extends BasicMessage implements Serializable {
    @NotNull(message = "The GoodID cannot be null.")
    @NotEmpty(message = "The GoodID cannot be empty.")
    @ValidGoodID
    String goodID;

    public GoodDataMessage(long timestamp,
                           String requestID,
                           String operation,
                           String from,
                           String to,
                           String signature,
                           String goodID) {
        super(timestamp, requestID, operation, from, to, signature);
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
