package hds.client.domain;

import hds.security.CryptoUtils;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.OwnerDataMessage;
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
 * The type get intention to sell callable performs a PUT request to the end point /stateOfGood of a notary replica
 * {@link hds.security.msgtypes.BasicMessage}
 * {@link hds.security.msgtypes.OwnerDataMessage}
 * @author Diogo Vilela
 * @author Francisco Barros
 * @author Rafael Ribeiro
 */
public class IntentionToSellCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "intentionToSell";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final OwnerDataMessage message;
    private final String replicaId;

    /**
     * Instantiates a new Intention to sell callable.
     *
     * @param timestamp the timestamp used by the notary to verify freshness of the message
     * @param requestId the request id used by the notary server to cache responses in case of lost messages over the network
     * @param replicaId the replica id to whom the client wishes to send the request
     * @param goodId    the good id the client wishes to sell
     * @param wts       the write timestamp associated with this write operation in (1, N) Regular Register paradigm
     * @param onSale    explicitly telling the notary the client wants to put this goodId on sale
     */
    public IntentionToSellCallable(long timestamp,
                                   String requestId,
                                   String replicaId,
                                   String goodId,
                                   int wts,
                                   Boolean onSale) {

        this.replicaId = replicaId;
        try {
            byte[] writeOnGoodsSignature = newWriteOnGoodsDataSignature(goodId, onSale, wts);
            this.message = newOwnerDataMessage(
                    timestamp, requestId, replicaId, goodId, wts, onSale, bytesToBase64String(writeOnGoodsSignature)
            );
        } catch (JSONException | SignatureException exc) {
            throw new RuntimeException(exc.getMessage());

        }
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
        return (BasicMessage) getResponseMessage(connection, Expect.WRITE_RESPONSE);
    }

    /**
     * Helper method that creates a signature over <goodId, onSale, wts> for (1, N) Regular Registers implementation
     * @param goodId    String with the name of the goodId the client wishes to sell
     * @param onSale    Boolean indicates the server that the client wishes to put goodId on sale, TRUE by default
     * @param wts       long timestamp representing an java epoch from seconds, {@link hds.security.DateUtils#generateTimestamp()}
     * @return byte[] with the signature over the JSONObject of the three parameters
     * @throws JSONException
     * @throws SignatureException
     */
    private byte[] newWriteOnGoodsDataSignature(final String goodId, final Boolean onSale, final int wts)
            throws JSONException, SignatureException {

        byte[] rawData = newWriteOnGoodsData(goodId, onSale, getMyClientPort(), wts).toString().getBytes();
        return CryptoUtils.signData(getMyPrivateKey(), rawData);
    }

    /**
     * Helper method that instantiates a new OwnerDataMessage
     * @param timestamp             timestamp representing an java epoch from seconds
     * @param requestId             a unique identifier used by notary replicas to return cached responses in case of
     *                              loss of messages over the network
     * @param to                    replica to where the client wishes to send this request message
     * @param goodId                the identifier this clients wishes to sell
     * @param wts                   timestamp representing an java epoch from seconds unlike timestamp, wts is explicitly
     *                              used to implement (1,N) regular registers behaviour
     * @param onSale                explicitly indicates to the server that the client wishes to put goodId on sale, TRUE by default
     * @param writeOnGoodsSignature String representing a signature over <goodId, wts, onSale> fields in base64 used for regular registers
     * @return {@link hds.security.msgtypes.OwnerDataMessage}
     */
    private OwnerDataMessage newOwnerDataMessage(final long timestamp,
                                                 final String requestId,
                                                 final String to,
                                                 final String goodId,
                                                 final int wts,
                                                 final Boolean onSale,
                                                 final String writeOnGoodsSignature) {

        return new OwnerDataMessage(
                timestamp,
                requestId,
                OPERATION,
                getMyClientPort(), // from
                to,
                "",
                goodId,
                getMyClientPort(), // owner
                wts,
                onSale,
                writeOnGoodsSignature
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
