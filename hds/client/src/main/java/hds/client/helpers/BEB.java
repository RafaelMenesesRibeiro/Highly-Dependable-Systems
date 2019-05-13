package hds.client.helpers;

import hds.security.msgtypes.BasicMessage;

import java.util.List;

public class BEB {
    private static BEB INSTANCE;

    final List<String> replicas;
    final Integer numberOfReplicas;

    private BEB(List<String> replicas) {
        this.replicas = replicas;
        this.numberOfReplicas = replicas.size();
    }

    public static BEB init(List<String> replicas) {
        if (INSTANCE == null) {
            INSTANCE = new BEB(replicas);
        }
        return INSTANCE;
    }

    public BasicMessage broadcast(BasicMessage message) {
        return null; // TODO
    }
}
