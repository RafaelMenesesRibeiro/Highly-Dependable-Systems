package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidClientID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ReadWtsResponse extends BasicMessage implements Serializable {
    @NotNull(message = "The clientID cannot be null.")
    @NotEmpty(message = "The clientID cannot be empty.")
    @ValidClientID
    private String clientID;

    @NotNull(message = "The readID cannot be null.")
    private int rid;

    public ReadWtsResponse(long timestamp,
                           String requestID,
                           String operation,
                           String from,
                           String to,
                           String signature,
                           String clientID,
                           int rid)
    {
        super(timestamp, requestID, operation, from, to, signature);
        this.clientID = clientID;
        this.rid = rid;
    }

    public ReadWtsResponse() {
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    @Override
    public String toString() {
        return "ReadWtsResponse{" +
                "clientID='" + clientID + '\'' +
                ", rid=" + rid +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
