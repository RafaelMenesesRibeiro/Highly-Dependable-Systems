package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getMyClientPort;
import static hds.client.helpers.ClientProperties.getMyPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.setMessageSignature;

public class RequestChallengeCallable implements Callable<BasicMessage> {
	private static final String OPERATION = "requestChallenge";
	private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
	private final BasicMessage message;
	private final String replicaID;

	public RequestChallengeCallable(long timestamp, String replicaId, String requestId) {
		this.replicaID = replicaId;
		this.message = newRequestChallengeMessage(timestamp, replicaId, requestId);
	}

	@Override
	public BasicMessage call() throws Exception {
		setMessageSignature(getMyPrivateKey(), message);
		HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaID, OPERATION));
		sendPostRequest(connection, newJSONObject(message));
		return (BasicMessage) getResponseMessage(connection, Expect.CHALLENGE_REQUEST_RESPONSE);
	}

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
