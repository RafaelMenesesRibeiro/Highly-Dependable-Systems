package hds.client.domain;

import hds.security.CryptoUtils;
import hds.security.DateUtils;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.GoodStateResponse;
import hds.security.msgtypes.OwnerDataMessage;
import hds.security.msgtypes.WriteBackMessage;
import org.json.JSONException;

import java.net.HttpURLConnection;
import java.security.SignatureException;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getMyClientPort;
import static hds.client.helpers.ClientProperties.getMyPrivateKey;
import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.SecurityManager.newWriteOnGoodsData;
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
public class WriteBackCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "writeBack";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final BasicMessage message;
    private final String replicaId;

    /**
     * Instantiates a new Intention to sell callable.
     *
     * @param timestamp the timestamp used by the notary to verify freshness of the message
     * @param requestId the request id used by the notary server to cache responses in case of lost messages over the network
     * @param replicaId the replica id to whom the client wishes to send the request
     * @param highestGoodState      a replica signed response containing the highest good state the client found on a read list
     * @param highestOwnershipState a replica signed response containing the highest ownership state the client found on a read list
     */
    public WriteBackCallable(long timestamp,
                             String requestId,
                             String replicaId,
                             int rid,
                             GoodStateResponse highestGoodState,
                             GoodStateResponse highestOwnershipState) {

        this.replicaId = replicaId;
        this.message = newWriteBackMessage(timestamp, requestId, replicaId, rid, highestGoodState, highestOwnershipState);
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
     * Helper method that instantiates a new OwnerDataMessage
     * @param timestamp             timestamp representing an java epoch from seconds
     * @param requestId             a unique identifier used by notary replicas to return cached responses in case of loss of messages over the network
     * @param to                    replica to where the client wishes to send this request message
     * @param highestGoodState      a replica signed response containing the highest good state the client found on a read list
     * @param highestOwnershipState a replica signed response containing the highest ownership state the client found on a read list
     * @return {@link WriteBackMessage}
     */
    private WriteBackMessage newWriteBackMessage(final long timestamp,
                                                 final String requestId,
                                                 final String to,
                                                 final int rid,
                                                 final GoodStateResponse highestGoodState,
                                                 final GoodStateResponse highestOwnershipState) {

        return new WriteBackMessage(
                timestamp,
                requestId,
                OPERATION,
                getMyClientPort(),
                to,
                "",
                rid,
                highestGoodState,
                highestOwnershipState
        );
    }

    @Override
    public String toString() {
        return "IntentionToSellCallable{" +
                "message=" + message.toString() +
                ", notaryReplicaId='" + replicaId + '\'' +
                '}';
    }
}
