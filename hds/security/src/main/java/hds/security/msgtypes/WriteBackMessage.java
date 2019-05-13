package hds.security.msgtypes;

import java.io.Serializable;

public class WriteBackMessage extends BasicMessage implements Serializable {
    public static final String OPERATION = "writeBack";

    private int rid;
    private GoodStateResponse highestGoodState;
    private GoodStateResponse highestOwnershipState;

    public WriteBackMessage(long timestamp,
                            String requestID,
                            String from,
                            String to,
                            String signature,
                            int rid,
                            GoodStateResponse highestGoodState,
                            GoodStateResponse highestOwnershipState) {

        super(timestamp, requestID, OPERATION, from, to, signature);
        this.rid = rid;
        this.highestGoodState = highestGoodState;
        this.highestOwnershipState = highestOwnershipState;
    }

    public WriteBackMessage() {

    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public GoodStateResponse getHighestGoodState() {
        return highestGoodState;
    }

    public void setHighestGoodState(GoodStateResponse highestGoodState) {
        this.highestGoodState = highestGoodState;
    }

    public GoodStateResponse getHighestOwnershipState() {
        return highestOwnershipState;
    }

    public void setHighestOwnershipState(GoodStateResponse highestOwnershipState) {
        this.highestOwnershipState = highestOwnershipState;
    }
    
    @Override
    public String toString() {
        return "WriteBackMessage{" +
                "rid=" + rid +
                ", highestGoodState=" + highestGoodState.toString() +
                ", highestOwnershipState=" + highestOwnershipState.toString() +
                ", requestID='" + requestID + '\'' +
                ", operation='" + OPERATION + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
