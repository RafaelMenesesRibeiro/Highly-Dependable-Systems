package hds.security.msgtypes;

import hds.security.msgtypes.BasicMessage;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class WriteBackResponse extends BasicMessage implements Serializable {
    @NotNull(message = "The read id cannot be null.")
    private int rid;

    public WriteBackResponse(long timestamp,
                             String requestID,
                             String operation,
                             String from,
                             String to,
                             String signature,
                             int rid) {

        super(timestamp, requestID, operation, from, to, signature);
        this.rid = rid;
    }

    public WriteBackResponse() {

    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    @Override
    public String toString() {
        return "WriteBackResponse{" +
                "rid=" + rid +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
