package hds.client.domain;

import hds.security.CryptoUtils;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.OwnerDataMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.security.SignatureException;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getPort;
import static hds.client.helpers.ClientProperties.getPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.signData;
import static hds.security.SecurityManager.*;

public class IntentionToSellCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "intentionToSell";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final OwnerDataMessage message;
    private final String replicaId;

    public IntentionToSellCallable(long timestamp,
                                   String requestId,
                                   String replicaId,
                                   String goodId,
                                   int logicalTimestamp,
                                   Boolean onSale) {

        this.replicaId = replicaId;
        try {
            byte[] writeOnGoodsSignature = newWriteOnGoodsDataSignature(goodId, onSale, logicalTimestamp);
            this.message = newOwnerDataMessage(
                    timestamp, requestId, replicaId, goodId, logicalTimestamp, onSale, bytesToBase64String(writeOnGoodsSignature)
            );
        } catch (JSONException | SignatureException exc) {
            throw new RuntimeException(exc.getMessage());

        }

    }

    @Override
    public BasicMessage call() throws Exception {
        setMessageSignature(getPrivateKey(), message);
        HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaId, OPERATION));
        sendPostRequest(connection, newJSONObject(message));
        return (BasicMessage) getResponseMessage(connection, Expect.BASIC_MESSAGE);
    }

    private byte[] newWriteOnGoodsDataSignature(final String goodId, final Boolean onSale, final int logicalTimestamp)
            throws JSONException, SignatureException {

        byte[] rawData = newWriteOnGoodsData(goodId, onSale, getPort(), logicalTimestamp).toString().getBytes();
        return CryptoUtils.signData(getPrivateKey(), rawData);
    }

    private OwnerDataMessage newOwnerDataMessage(final long timestamp,
                                                 final String requestId,
                                                 final String to,
                                                 final String goodId,
                                                 final int logicalTimestamp,
                                                 final Boolean onSale,
                                                 final String writeOnGoodsSignature) {

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
                writeOnGoodsSignature
        );
    }
}
