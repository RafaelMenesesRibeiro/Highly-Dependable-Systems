package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ConnectionManager.*;

public class GetStateOfGoodCallable implements Callable<BasicMessage> {
    private static final String OPERATION = "stateOfGood";
    private static final String REQUEST_ENDPOINT = "http://localhost:%s/%s?goodID=%s&readID=%s";
    private final String address;
    private final String replicaId;

    public GetStateOfGoodCallable(String replicaId, String goodId, int readId) {
        this.replicaId = replicaId;
        this.address = String.format(REQUEST_ENDPOINT, replicaId, OPERATION, goodId, String.valueOf(readId));
    }

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
