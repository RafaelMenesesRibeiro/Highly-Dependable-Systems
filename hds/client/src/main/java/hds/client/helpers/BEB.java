package hds.client.helpers;

import hds.security.msgtypes.BasicMessage;

import java.util.List;

public class BEB {
    private static BEB INSTANCE;

    private final ONBAR onbar;

    private final List<String> replicas;
    private final Integer numberOfReplicas;


    private BEB(ONBAR onbar, List<String> replicas) {
        this.onbar = onbar;
        this.replicas = replicas;
        this.numberOfReplicas = replicas.size();
    }

    public static BEB init(ONBAR onbar, List<String> replicas) {
        if (INSTANCE == null) {
            INSTANCE = new BEB(onbar, replicas);
        }
        return INSTANCE;
    }

    public BasicMessage broadcast(BasicMessage message) {
        return null; // TODO
    }
}
