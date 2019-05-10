package hds.client.domain;

import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.OwnerDataMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getPort;
import static hds.client.helpers.ClientProperties.getPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.setMessageSignature;

public class IntentionToSellCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "intentionToSell";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final String replicaId;
    private final OwnerDataMessage message;

    public IntentionToSellCallable(long timestamp, String requestId, String replicaId, String goodId) {
        this.replicaId = replicaId;
        this.message = newOwnerDataMessage(timestamp, requestId, replicaId, goodId);
    }

    @Override
    public BasicMessage call() throws Exception {
        setMessageSignature(getPrivateKey(), message);
        HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaId, OPERATION));
        sendPostRequest(connection, newJSONObject(message));
        return (BasicMessage) getResponseMessage(connection, Expect.BASIC_MESSAGE);
    }

    private OwnerDataMessage newOwnerDataMessage(long timestamp, String requestId, String to, String goodId) {
        return new OwnerDataMessage(
                timestamp,
                requestId,
                OPERATION,
                getPort(),
                to,
                "",
                goodId,
                getPort()
        );
    }
}
