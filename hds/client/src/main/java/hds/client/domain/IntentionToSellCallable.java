package hds.client.domain;

import hds.security.CryptoUtils;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.OwnerDataMessage;
import org.json.JSONException;

import java.net.HttpURLConnection;
import java.security.SignatureException;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getMyClientPort;
import static hds.client.helpers.ClientProperties.getMyClientPort;
import static hds.client.helpers.ClientProperties.getPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.SecurityManager.newWriteOnGoodsData;
import static hds.security.SecurityManager.setMessageSignature;

public class IntentionToSellCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "intentionToSell";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final OwnerDataMessage message;
    private final String replicaId;

    public IntentionToSellCallable(long timestamp,
                                   String requestId,
                                   String replicaId,
                                   String goodId,
                                   long wts,
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

    @Override
    public BasicMessage call() throws Exception {
        setMessageSignature(getPrivateKey(), message);
        HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaId, OPERATION));
        sendPostRequest(connection, newJSONObject(message));
        return (BasicMessage) getResponseMessage(connection, Expect.WRITE_RESPONSE);
    }

    private byte[] newWriteOnGoodsDataSignature(final String goodId, final Boolean onSale, final long wts)
            throws JSONException, SignatureException {

        byte[] rawData = newWriteOnGoodsData(goodId, onSale, getMyClientPort(), wts).toString().getBytes();
        return CryptoUtils.signData(getPrivateKey(), rawData);
    }

    private OwnerDataMessage newOwnerDataMessage(final long timestamp,
                                                 final String requestId,
                                                 final String to,
                                                 final String goodId,
                                                 final long wts,
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
