package hds.security.msgtypes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class WriteBackMessage extends BasicMessage implements Serializable {

    private String readOperation;
    private BasicMessage readResponse;

    public WriteBackMessage(long timestamp,
                            String requestID,
                            String operation,
                            String from,
                            String to,
                            String signature,
                            String readOperation,
                            BasicMessage readResponse) {
        super(timestamp, requestID, operation, from, to, signature);
        this.readOperation = readOperation;
        this.readResponse = readResponse;
    }

    public WriteBackMessage() {

    }

    public String getReadOperation() {
        return readOperation;
    }

    public void setReadOperation(String readOperation) {
        this.readOperation = readOperation;
    }

    public BasicMessage getReadResponse() {
        return readResponse;
    }

    public void setReadResponse(BasicMessage readResponse) {
        this.readResponse = readResponse;
    }

    @Override
    public String toString() {
        return "WriteBackMessage{" +
                "readOperation='" + readOperation + '\'' +
                ", readResponse=" + readResponse +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }

    public static BasicMessage getCastedReadResponse(String readOperation, String readResponseString) {
        try {
            switch (readOperation) {
                case "stateOfGood":
                    return new ObjectMapper().readValue(readResponseString, GoodStateResponse.class);
                default:
                    return null;
            }
        } catch (IOException exc) {
            return null;
        }

    }
}
