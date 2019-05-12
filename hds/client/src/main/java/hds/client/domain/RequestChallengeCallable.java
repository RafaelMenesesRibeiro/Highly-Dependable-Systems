package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getMyClientPort;
import static hds.client.helpers.ClientProperties.getMyPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.setMessageSignature;

public class RequestChallengeCallable implements Callable<BasicMessage> {
	private static final String OPERATION = "requestData";
	private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
	private final BasicMessage message;
	private final String replicaID;

	public RequestChallengeCallable(long timestamp, String requestID, String replicaID) {
		this.replicaID = replicaID;
		this.message = newRequestChallengeMessage(timestamp, requestID, replicaID);
	}

	@Override
	public BasicMessage call() throws Exception {
		setMessageSignature(getMyPrivateKey(), message);
		HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaID, OPERATION));
		sendPostRequest(connection, newJSONObject(message));
		return (BasicMessage) getResponseMessage(connection, Expect.CHALLENGE_REQUEST_RESPONSE);
	}

	private BasicMessage newRequestChallengeMessage(long timestamp, String requestID, String replicaID) {
		return new BasicMessage(timestamp, requestID, OPERATION, getMyClientPort(), replicaID, "");
	}

	@Override
	public String toString() {
		return "RequestChallengeCallable{" +
				"message=" + message.toString() +
				", notaryReplicaId='" + replicaID + '\'' +
				'}';
	}
}
