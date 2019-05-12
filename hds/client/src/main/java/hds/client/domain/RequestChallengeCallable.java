package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getMyClientPort;
import static hds.client.helpers.ClientProperties.getMyPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.setMessageSignature;

/**
 * The type request challenge callable performs a GET request to the end point /requestChallenge of a notary replica
 * See also:
 * {@link hds.security.msgtypes.BasicMessage}
 * {@link hds.security.DateUtils#generateTimestamp()}
 * @author Diogo Vilela
 * @author Francisco Barros
 * @author Rafael Ribeiro
 */
public class RequestChallengeCallable implements Callable<BasicMessage> {
	private static final String OPERATION = "requestChallenge";
	private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
	private final BasicMessage message;
	private final String replicaID;

	/**
	 * Instantiates a new Request challenge callable.
	 *
	 * @param timestamp the timestamp used by the notary to verify freshness of the message
	 * @param requestId the request id used by the notary server to cache responses in case of lost messages over the network
	 * @param replicaId the replica id to whom the client wishes to send the request
	 */
	public RequestChallengeCallable(long timestamp, String replicaId, String requestId) {
		this.replicaID = replicaId;
		this.message = newRequestChallengeMessage(timestamp, replicaId, requestId);
	}

	/**
	 * Executes this callable
	 * @return BasicMessage
	 * @throws Exception which can be of type SignatureException, IOException, JsonProcessingException and JSONException
	 */
	@Override
	public BasicMessage call() throws Exception {
		setMessageSignature(getMyPrivateKey(), message);
		HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaID, OPERATION));
		sendPostRequest(connection, newJSONObject(message));
		return (BasicMessage) getResponseMessage(connection, Expect.CHALLENGE_REQUEST_RESPONSE);
	}

	/**
	 * Helper method that instantiates a new BasicMessage
	 * @param timestamp    timestamp representing an java epoch from seconds
	 * @param replicaId    the identifier representing the port where the replica is listening on
	 * @param requestId    the request id used by the notary server to cache responses in case of lost messages over the network
	 * @return {@link hds.security.msgtypes.BasicMessage}
	 */
	private BasicMessage newRequestChallengeMessage(long timestamp, String replicaId, String requestId) {
		return new BasicMessage(timestamp, requestId, OPERATION, getMyClientPort(), replicaId, "");
	}

	@Override
	public String toString() {
		return "RequestChallengeCallable{" +
				"message=" + message.toString() +
				", notaryReplicaId='" + replicaID + '\'' +
				'}';
	}
}
