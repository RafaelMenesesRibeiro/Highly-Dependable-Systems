package hds.client.domain;

import hds.client.helpers.ClientProperties;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.SaleRequestMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ClientProperties.getPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.setMessageSignature;
import static hds.security.SecurityManager.setMessageWrappingSignature;

public class TransferGoodCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "transferGood";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s";
    private final String replicaId;
    private final ApproveSaleRequestMessage message;

    public TransferGoodCallable(long timestamp, String replicaId, SaleRequestMessage requestMessage) {
        this.replicaId = replicaId;
        this.message = newApproveSaleRequestMessage(timestamp, replicaId, requestMessage);
    }

    @Override
    public BasicMessage call() throws Exception {
        setMessageWrappingSignature(getPrivateKey(), message);
        HttpURLConnection connection = initiatePOSTConnection (String.format(REQUEST_ENDPOINT, replicaId, OPERATION));
        sendPostRequest(connection, newJSONObject(message));
        return (BasicMessage) getResponseMessage(connection, Expect.SALE_CERT_RESPONSE);
    }

    private ApproveSaleRequestMessage newApproveSaleRequestMessage(long timestamp,
                                                                   String replicaId,
                                                                   SaleRequestMessage requestMessage) {
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
                requestMessage.getLogicalTimestamp(),
                requestMessage.getOnSale(),
                requestMessage.getWriteOnGoodsSignature(),
                requestMessage.getwriteOnOwnershipsSignature(),
                timestamp,
                OPERATION,
                ClientProperties.getPort(),
                replicaId,
                ""
        );
    }
}
