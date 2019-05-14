package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidClientID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ReadWtsResponse extends BasicMessage implements Serializable {
    @NotNull(message = "The readID cannot be null.")
    private int rid;

    @NotNull(message = "The readID cannot be null.")
    private long wts;

    public ReadWtsResponse(long timestamp,
                           String requestID,
                           String operation,
                           String from,
                           String to,
                           String signature,
                           int rid,
                           long wts)
    {
        super(timestamp, requestID, operation, from, to, signature);
        this.rid = rid;
        this.wts = wts;
    }

    public ReadWtsResponse() {
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public long getWts() {
        return wts;
    }

    public void setWts(long wts) {
        this.wts = wts;
    }

    @Override
    public String toString() {
        return "ReadWtsResponse{" +
                "rid=" + rid +
                ", wts=" + wts +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
