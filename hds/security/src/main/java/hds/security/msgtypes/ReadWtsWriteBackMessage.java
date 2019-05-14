package hds.security.msgtypes;

import java.io.Serializable;

public class ReadWtsWriteBackMessage extends BasicMessage implements Serializable {
    public static final String OPERATION = "readWriteBack";

    private int rid;
    private ReadWtsResponse highest;

    public ReadWtsWriteBackMessage(long timestamp,
                                   String requestID,
                                   String operation,
                                   String from,
                                   String to,
                                   String signature,
                                   int rid,
                                   ReadWtsResponse highest) {

        super(timestamp, requestID, operation, from, to, signature);
        this.rid = rid;
        this.highest = highest;
    }

    public ReadWtsWriteBackMessage() {

    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public ReadWtsResponse getHighest() {
        return highest;
    }

    public void setHighest(ReadWtsResponse highest) {
        this.highest = highest;
    }

    @Override
    public String toString() {
        return "ReadWtsWriteBackMessage{" +
                "rid=" + rid +
                ", highest=" + highest +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
