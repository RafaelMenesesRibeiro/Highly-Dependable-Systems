package hds.client.domain;

import hds.security.helpers.managers.ConnectionManager;
import hds.security.msgtypes.BasicMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.security.helpers.managers.ConnectionManager.getResponseMessage;
import static hds.security.helpers.managers.ConnectionManager.initiateGETConnection;

/**
 * The type request get wts callable performs a GET request to the end point /getCurrentTimestamp of a notary replica
 * See also:
 * {@link hds.security.msgtypes.BasicMessage}
 * {@link hds.security.msgtypes.ReadWtsResponse}
 * {@link hds.security.DateUtils#generateTimestamp()}
 * @author Diogo Vilela
 * @author Francisco Barros
 * @author Rafael Ribeiro
 */
public class ReadWtsCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "getCurrentTimestamp";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s?clientID=%s&readID=%s";
    private final String address;
    private final String replicaId;

    /**
     * Instantiates a new get write time stamp callable
     *
     * @param replicaId the replica id to whom the client wishes to ask about the goodId
     * @param clientId  the string that uniquely identifies this client
     * @param rid       a request identifier used only by the server to ensure correct processing of incoming replies in asynchronous operation
     */
    public ReadWtsCallable(String replicaId, String clientId, int rid) {
        this.replicaId = replicaId;
        this.address = String.format(REQUEST_ENDPOINT, replicaId, OPERATION, clientId, String.valueOf(rid));
    }

    /**
     * Executes this callable
     * @return BasicMessage
     * @throws Exception which can be of type SignatureException, IOException, JsonProcessingException and JSONException
     */
    @Override
    public BasicMessage call() throws Exception {
        HttpURLConnection connection = initiateGETConnection(address);
        return (BasicMessage) getResponseMessage(connection, ConnectionManager.Expect.READ_WTS_RESPONSE);
    }

    @Override
    public String toString() {
        return "ReadWtsCallable{" +
                "address='" + address + '\'' +
                ", notaryReplicaId='" + replicaId + '\'' +
                '}';
    }
}
