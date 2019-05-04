package hds.client.helpers;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import static hds.security.ResourceManager.getPrivateKeyFromResource;

public class ClientProperties {
    public static final int HDS_NOTARY_REPLICAS_FIRST_PORT = 9000;
    public static final String HDS_BASE_HOST = "http://localhost:";

    private static String portId;
    private static String maxPortId;
    private static ArrayList<String> notaryReplicasPorts;
    private static PrivateKey privateKey;

    private ClientProperties() {}

    public static String getPort() {
        return portId;
    }

    public static void setPort(String portId) {
        if (ClientProperties.portId != null) { throw new RuntimeException("port is 'final'"); }
        ClientProperties.portId = portId;
        try {
            ClientProperties.privateKey = getPrivateKeyFromResource(portId);
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            System.out.println("Error loading privateKey from resources");
            System.exit(-1);
        }
    }

    public static String getMaxPortId() {
        return maxPortId;
    }

    public static void setMaxPortId(String maxPortId) {
        if (ClientProperties.maxPortId != null) { throw new RuntimeException("maxPortId is 'final'"); }
        ClientProperties.maxPortId = maxPortId;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static ArrayList<String> getNotaryReplicas() {
        return ClientProperties.notaryReplicasPorts;
    }

    public static void updateNotaryReplicas(ArrayList<String> newSet) {
        notaryReplicasPorts = newSet;
    }

    public static void removeNotaryReplica(String replicaPort) {
        ClientProperties.notaryReplicasPorts.remove(replicaPort);
    }

    public static void initializeNotaryReplicasPortsList(int maxServerPort) {
        ArrayList<String> replicas = new ArrayList<>();
        for (int replicaPort = HDS_NOTARY_REPLICAS_FIRST_PORT; replicaPort <= maxServerPort; replicaPort++) {
            replicas.add("" + replicaPort);
        }
        ClientProperties.notaryReplicasPorts = replicas;
    }

    public static void print(String msg) {
        System.out.println("[o] " + msg);
    }

    public static void printError(String msg) {
        System.out.println("    [x] " + msg);
    }
}
