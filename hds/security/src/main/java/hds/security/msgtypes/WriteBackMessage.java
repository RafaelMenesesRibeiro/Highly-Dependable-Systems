package hds.security.msgtypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;

import static hds.security.helpers.managers.ConnectionManager.getJSONStringFromHttpResponse;

public class WriteBackMessage extends BasicMessage implements Serializable {
    public static final String GET_STATE_OF_GOOD_OPERATION = "getStateOfGood";

    private BasicMessage readResponse;

    public WriteBackMessage(long timestamp,
                            String requestID,
                            String operation,
                            String from,
                            String to,
                            String signature,
                            BasicMessage readResponse) {
        super(timestamp, requestID, operation, from, to, signature);
        this.readResponse = readResponse;
    }

    public WriteBackMessage() {

    }

    public BasicMessage getReadResponse() {
        return readResponse;
    }

    public void setReadResponse(BasicMessage readResponse) {
        this.readResponse = readResponse;
    }

    public static BasicMessage getCastedReadResponse(String readOperation, String readResponseString) {
        try {
            switch (readOperation) {
                case GET_STATE_OF_GOOD_OPERATION:
                    return new ObjectMapper().readValue(readResponseString, GoodStateResponse.class);
                default:
                    return null;
            }
        } catch (IOException exc) {
            return null;
        }
    }

    public static String jsonString(HttpURLConnection connection) throws IOException {
        return getJSONStringFromHttpResponse(connection);
    }

    public static BasicMessage getHighValue(HttpURLConnection connection) throws JSONException, IOException {
        String jsonString = getJSONStringFromHttpResponse(connection);
        JSONObject jsonObject = new JSONObject(jsonString);

        if (jsonObject.has("reason")) {
            return null;
        } else {
            String readOperation = jsonObject.getString("readOperation");
            String readResponse = jsonObject.getJSONObject("readResponse").toString();
            return getCastedReadResponse(readOperation, readResponse);
        }
    }

    @Override
    public String toString() {
        return "WriteBackMessage{" +
                "readResponse=" + readResponse +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
