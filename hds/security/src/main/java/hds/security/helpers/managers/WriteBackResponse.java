package hds.security.helpers.managers;

import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.WriteBackMessage;

import java.io.Serializable;

public class WriteBackResponse extends BasicMessage implements Serializable {
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
                '}';
    }
}
