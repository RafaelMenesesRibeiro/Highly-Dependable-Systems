package hds.security.msgtypes;

import java.io.Serializable;

public class WriteBackMessage extends BasicMessage implements Serializable {

    private GoodStateResponse highestGoodState;
    private GoodStateResponse highestOwnershipState;

    public WriteBackMessage(long timestamp,
                            String requestID,
                            String operation,
                            String from,
                            String to,
                            String signature,
                            GoodStateResponse highestGoodState,
                            GoodStateResponse highestOwnershipState) {

        super(timestamp, requestID, operation, from, to, signature);
        this.highestGoodState = highestGoodState;
        this.highestOwnershipState = highestOwnershipState;
    }

    public WriteBackMessage() {

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
                "highestGoodState=" + highestGoodState.toString() +
                ", highestOwnershipState=" + highestOwnershipState.toString() +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
