package hds.client.domain;

import hds.security.msgtypes.*;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getMyClientPort;
import static hds.client.helpers.ClientProperties.getMyPrivateKey;
import static hds.security.SecurityManager.setMessageSignature;
import static hds.security.helpers.managers.ConnectionManager.*;

/**
 * The type get intention to sell callable performs a PUT request to the end point /writeBack of a notary replica after a doing a GET on /stateOfGood
 * {@link BasicMessage}
 * {@link GoodStateResponse}
 * {@link WriteBackMessage}
 * @author Diogo Vilela
 * @author Francisco Barros
 * @author Rafael Ribeiro
 */
public class ReadWtsWriteBackCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "readWtsWriteBack";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final BasicMessage message;
    private final String replicaId;

    /**
     * Instantiates a new Intention to sell callable.
     *
     * @param timestamp the timestamp used by the notary to verify freshness of the message
     * @param requestId the request id used by the notary server to cache responses in case of lost messages over the network
     * @param replicaId the replica id to whom the client wishes to send the request
     * @param highest   a replica signed response containing the highest read wts response the client found on a read list
     */
    public ReadWtsWriteBackCallable(long timestamp,
                                    String requestId,
                                    String replicaId,
                                    int rid,
                                    ReadWtsResponse highest) {

        this.replicaId = replicaId;
        this.message = newReadWtsWriteBackMessage(timestamp, requestId, replicaId, rid, highest);
    }

    /**
     * Executes this callable
     * @return BasicMessage
     * @throws Exception which can be of type SignatureException, IOException, JsonProcessingException and JSONException
     */
    @Override
    public BasicMessage call() throws Exception{
        setMessageSignature(getMyPrivateKey(), message);
        HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaId, OPERATION));
        sendPostRequest(connection, newJSONObject(message));
        return (BasicMessage) getResponseMessage(connection, Expect.WRITE_BACK_RESPONSE);
    }

    /**
     * Helper method that instantiates a new ReadWtsWriteBackMessage
     * @param timestamp   timestamp representing an java epoch from seconds
     * @param requestId   a unique identifier used by notary replicas to return cached responses in case of loss of messages over the network
     * @param to          replica to where the client wishes to send this request message
     * @param highest     a replica signed response containing the highest read wts response the client found on a read list
     * @return {@link ReadWtsWriteBackMessage}
     */
    private ReadWtsWriteBackMessage newReadWtsWriteBackMessage(final long timestamp,
                                                        final String requestId,
                                                        final String to,
                                                        final int rid,
                                                        final ReadWtsResponse highest) {

        return new ReadWtsWriteBackMessage(
                timestamp,
                requestId,
                OPERATION,
                getMyClientPort(),
                to,
                "",
                rid,
                highest
        );
    }

    @Override
    public String toString() {
        return "ReadWtsWriteBackCallable{" +
                "message=" + message.toString() +
                ", notaryReplicaId='" + replicaId + '\'' +
                '}';
    }
}
