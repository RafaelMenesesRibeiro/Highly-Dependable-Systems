package hds.client.helpers;

import hds.security.msgtypes.BasicMessage;
import javafx.util.Pair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ONRR {
    private static ONRR INSTANCE;

    private Pair<Integer, JSONObject> currentValue;
    private AtomicInteger wts;
    private AtomicInteger rid;
    private AtomicInteger acks;
    private List<BasicMessage> readList;

    public static ONRR init(int numberOfReplicas) {
        if (INSTANCE == null) {
            INSTANCE = new ONRR(numberOfReplicas);
        }
        return INSTANCE;
    }

    private ONRR(int numberOfReplicas) {
        this.readList = Arrays.asList(new BasicMessage[numberOfReplicas]);
        this.acks = new AtomicInteger(0);
        this.rid = new AtomicInteger(0);
        this.wts = new AtomicInteger(0);
        this.currentValue = new Pair<>(wts.get(), new JSONObject());
    }

    public void write(JSONObject value) {
        wts.incrementAndGet();
    }
}
