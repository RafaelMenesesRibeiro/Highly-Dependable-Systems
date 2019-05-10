package hds.client.domain;

import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.OwnerDataMessage;

import java.net.HttpURLConnection;
import java.security.SignatureException;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getPort;
import static hds.client.helpers.ClientProperties.getPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.signData;
import static hds.security.SecurityManager.newWriteOperation;
import static hds.security.SecurityManager.setMessageSignature;

public class IntentionToSellCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "intentionToSell";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private static final Boolean onSale;

    private final OwnerDataMessage message;
    private final String replicaId;

    static {
        onSale = Boolean.TRUE;
    }

    public IntentionToSellCallable(long timestamp, String requestId, String replicaId, String goodId, int logicalTimestamp)
            throws SignatureException {

        this.replicaId = replicaId;

        byte[] writeOpSignature = signData(
                getPrivateKey(), newWriteOperation(onSale, getPort(), logicalTimestamp).toString().getBytes()
        );

        this.message = newOwnerDataMessage(
                timestamp, requestId, replicaId, goodId, logicalTimestamp, bytesToBase64String(writeOpSignature)
        );
    }

    @Override
    public BasicMessage call() throws Exception {
        setMessageSignature(getPrivateKey(), message);
        HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaId, OPERATION));
        sendPostRequest(connection, newJSONObject(message));
        return (BasicMessage) getResponseMessage(connection, Expect.BASIC_MESSAGE);
    }

    private OwnerDataMessage newOwnerDataMessage(long timestamp,
                                                 String requestId,
                                                 String to,
                                                 String goodId,
                                                 int logicalTimestamp,
                                                 String writeOperationJson) {

        return new OwnerDataMessage(
                timestamp,
                requestId,
                OPERATION,
                getPort(), // from
                to,
                "",
                goodId,
                getPort(), // owner
                logicalTimestamp,
                onSale,
                writeOperationJson
        );
    }
}
