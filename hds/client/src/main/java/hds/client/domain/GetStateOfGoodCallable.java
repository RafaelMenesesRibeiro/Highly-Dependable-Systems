package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ConnectionManager.*;

/**
 * The type get state of good callable performs a GET request to the end point /stateOfGood of a notary replica
 * @author Diogo Vilela
 * @author Francisco Barros
 * @author Rafael Ribeiro
 */
public class GetStateOfGoodCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "stateOfGood";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s?goodID=%s&readID=%s";
    private final String address;
    private final String replicaId;

    /**
     * Instantiates a new Get state of good callable.
     * @param replicaId the replica id to whom the client wishes to ask about the goodId
     * @param goodId    the good id the client wishes to know more about
     * @param readId    a request identifier used only by the server to ensure correct processing of incoming replies in asynchronous operation
     */
    public GetStateOfGoodCallable(String replicaId, String goodId, int readId) {
        this.replicaId = replicaId;
        this.address = String.format(REQUEST_ENDPOINT, replicaId, OPERATION, goodId, String.valueOf(readId));
    }

    /**
     * Executes this callable
     * @return BasicMessage
     * @throws Exception which can be of type SignatureException, IOException, JsonProcessingException and JSONException
     */
    @Override
    public BasicMessage call() throws Exception {
        HttpURLConnection connection = initiateGETConnection(address);
        return (BasicMessage) getResponseMessage(connection, Expect.GOOD_STATE_RESPONSE);
    }

    @Override
    public String toString() {
        return "GetStateOfGoodCallable{" +
                "address='" + address + '\'' +
                ", notaryReplicaId='" + replicaId + '\'' +
                '}';
    }
}
