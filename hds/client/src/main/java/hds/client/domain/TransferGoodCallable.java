package hds.client.domain;

import hds.client.helpers.ClientProperties;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.SaleRequestMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getMyPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.setMessageWrappingSignature;

/**
 * The type transfer good callable performs a PUT request to the end point /transferGood of a notary replica
 * @author Diogo Vilela
 * @author Francisco Barros
 * @author Rafael Ribeiro
 */
public class TransferGoodCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "transferGood";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final ApproveSaleRequestMessage message;
    private final String replicaId;


    /**
     * Instantiates a new Transfer good callable.
     *
     * @param timestamp         the timestamp
     * @param replicaId         the replica id
     * @param requestMessage    the request message
     * @param challengeResponse the challenge response
     */
    public TransferGoodCallable(long timestamp, String replicaId, SaleRequestMessage requestMessage, String challengeResponse) {
        this.replicaId = replicaId;
        this.message = newApproveSaleRequestMessage(timestamp, replicaId, requestMessage, challengeResponse);
    }

    /**
     * Executes this callable
     * @return BasicMessage
     * @throws Exception which can be of type SignatureException, IOException, JsonProcessingException and JSONException
     */
    @Override
    public BasicMessage call() throws Exception {
        setMessageWrappingSignature(getMyPrivateKey(), message);
        HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaId, OPERATION));
        sendPostRequest(connection, newJSONObject(message));
        return (BasicMessage) getResponseMessage(connection, Expect.SALE_CERT_RESPONSE);
    }

    /**
     * Helper method that instantiates a new ApproveSaleRequestMessage
     * @param timestamp         long timestamp representing an java epoch from seconds, {@link hds.security.DateUtils#generateTimestamp()}
     * @param replicaId         String identifier representing the port where the replica is listening on
     * @param requestMessage    {@link hds.security.msgtypes.SaleRequestMessage}
     * @param challengeResponse String with what is believed to be the solution for the computational challenge provided by the server
     * @return {@link hds.security.msgtypes.ApproveSaleRequestMessage}
     */
    private ApproveSaleRequestMessage newApproveSaleRequestMessage(long timestamp,
                                                                   String replicaId,
                                                                   SaleRequestMessage requestMessage,
                                                                   String challengeResponse) {

        return new ApproveSaleRequestMessage(
                requestMessage.getTimestamp(),
                requestMessage.getRequestID(),
                requestMessage.getOperation(),
                requestMessage.getFrom(),
                requestMessage.getTo(),
                requestMessage.getSignature(),
                requestMessage.getGoodID(),
                requestMessage.getBuyerID(),
                requestMessage.getSellerID(),
                requestMessage.getWts(),
                requestMessage.getOnSale(),
                requestMessage.getWriteOnGoodsSignature(),
                requestMessage.getWriteOnOwnershipsSignature(),
                timestamp,
                OPERATION,
                ClientProperties.getMyClientPort(),
                replicaId,
                "",
                challengeResponse
        );
    }

    @Override
    public String toString() {
        return "TransferGoodCallable{" +
                "message=" + message.toString() +
                ", notaryReplicaId='" + replicaId + '\'' +
                '}';
    }
}
