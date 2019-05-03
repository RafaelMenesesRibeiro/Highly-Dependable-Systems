package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import static hds.client.helpers.ConnectionManager.*;

public class GetStateOfGoodCallable implements Callable<BasicMessage> {
    private String address;

    public GetStateOfGoodCallable(String address) {
        this.address = address;
    }

    @Override
    public BasicMessage call() throws Exception {
        HttpURLConnection connection = initiateGETConnection(address);
        return getResponseMessage(connection, Expect.GOOD_STATE_RESPONSE);
    }
}
