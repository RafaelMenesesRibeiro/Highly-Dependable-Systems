package hds.client.helpers;

import hds.security.msgtypes.BasicMessage;
import javafx.util.Pair;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The class type for (1, N) Byzantine Atomic Registers with Read-Impose Write-Majority
 * Uses BestEffortBroadcast instance
 * Uses AuthenticatedLinks instance
 */
public class ONBAR {
    private static ONBAR INSTANCE;

    private final BEB beb;

    private Pair<Integer, JSONObject> record;
    private AtomicInteger wts;
    private AtomicInteger acks;
    private AtomicInteger rid;
    private List<BasicMessage> readList;
    private JSONObject readVal;
    private AtomicBoolean reading;
    /**
     * Instantiates a ONBAR object.
     * Has private protection because each JVM process should have a singleton ONBAR instance.
     * To obtain the instance call this class init method {@link hds.client.helpers.ONBAR#init}
     *
     * @return the onbar
     */
    private ONBAR(BEB bebInstance) {
        this.record = new Pair<>(0, new JSONObject());
        this.wts = new AtomicInteger(0);
        this.acks = new AtomicInteger(0);
        this.rid = new AtomicInteger(0);
        this.readList = Arrays.asList(new BasicMessage[ClientProperties.getReplicasList().size()]);
        this.readVal = null;
        this.reading = new AtomicBoolean(Boolean.FALSE);
        this.beb = BEB.init(this, ClientProperties.getReplicasList());
    }

    /**
     * Retrieves or creates a Singleton ONBAR instance
     *
     * @param bebInstance a best effort broadcast instance
     * @return a new onbar or the one already existing on this process's JVM.
     */
    public static ONBAR init(BEB bebInstance) {
        if (INSTANCE == null) {
            INSTANCE = new ONBAR(bebInstance);
        }
        return INSTANCE;
    }

    public void read(JSONObject readRequest) {
        rid.incrementAndGet();
        acks.set(0);
        readList = Arrays.asList(new BasicMessage[ClientProperties.getReplicasList().size()]);
        reading.set(Boolean.TRUE);
        beb.broadcast(readRequest, rid.get());
    }

    public void deliver() {
        
    }
}
