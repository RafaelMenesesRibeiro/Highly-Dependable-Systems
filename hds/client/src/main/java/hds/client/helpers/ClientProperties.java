package hds.client.helpers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hds.security.ResourceManager.getPrivateKeyFromResource;

public class ClientProperties {
    // TODO Introduce args for number of tolerated faults
    // TODO Introduce args for replicas using CC
    // TODO Make a function that returns a single list of server IDs... Both in the 9000 and 10000 slots
    public static final int HDS_NOTARY_REPLICAS_FIRST_PORT = 9000;
    public static final int HDS_NOTARY_REPLICAS_FIRST_CC_PORT = 10000;
    public static final String HDS_BASE_HOST = "http://localhost:";

    private static int maxFailures = 0;
    private static String myClientPort;
    private static String maxClientPort;
    private static ArrayList<String> regularReplicaIdList = new ArrayList<>();
    private static ArrayList<String> citizenReplicaIdList = new ArrayList<>();

    private static PrivateKey myPrivateKey;

    private ClientProperties() {}

    public static ArrayList<String> createIDList(int start, int end) {
        ArrayList<String> replicas = new ArrayList<>();
        for (int replicaPort = start; replicaPort <= end; replicaPort++) {
            replicas.add("" + replicaPort);
        }
        return replicas;
    }

    public static void initializeRegularReplicasIDList(int number) {
        int maxPort = HDS_NOTARY_REPLICAS_FIRST_PORT + number - 1;
        setRegularReplicaIdList(createIDList(HDS_NOTARY_REPLICAS_FIRST_PORT, maxPort));
    }

    public static void initializeCCReplicasIDList(int number) {
        int maxPort = HDS_NOTARY_REPLICAS_FIRST_CC_PORT + number - 1;
        setCitizenReplicaIdList(createIDList(HDS_NOTARY_REPLICAS_FIRST_CC_PORT, maxPort));
    }

    public static int getMaxFailures() {
        return maxFailures;
    }

    public static void setMaxFailures(int maxFailures) {
        ClientProperties.maxFailures = maxFailures;
    }

    public static String getMyClientPort() {
        return myClientPort;
    }

    public static void setMyClientPort(String myClientPort) {
        ClientProperties.myClientPort = myClientPort;
        try {
            ClientProperties.myPrivateKey = getPrivateKeyFromResource(myClientPort);
        }
        catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException ex) {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE, "Error loading privateKey from resources");
            System.exit(-1);
        }
    }

    public static String getMaxClientPort() {
        return maxClientPort;
    }

    public static void setMaxClientPort(String maxClientPort) {
        ClientProperties.maxClientPort = maxClientPort;
    }

    public static ArrayList<String> getRegularReplicaIdList() {
        return regularReplicaIdList;
    }

    public static void setRegularReplicaIdList(ArrayList<String> regularReplicaIdList) {
        ClientProperties.regularReplicaIdList = regularReplicaIdList;
    }

    public static ArrayList<String> getCitizenReplicaIdList() {
        return citizenReplicaIdList;
    }

    public static void setCitizenReplicaIdList(ArrayList<String> citizenReplicaIdList) {
        ClientProperties.citizenReplicaIdList = citizenReplicaIdList;
    }

    public static PrivateKey getMyPrivateKey() {
        return myPrivateKey;
    }

    public static void setMyPrivateKey(PrivateKey myPrivateKey) {
        ClientProperties.myPrivateKey = myPrivateKey;
    }

    public static int getMajorityThreshold() {
        int numberOfReplicas =
                ClientProperties.getRegularReplicaIdList().size() + ClientProperties.getCitizenReplicaIdList().size();
        return (numberOfReplicas + ClientProperties.getMaxFailures()) / 2;
    }

    public static ArrayList<String> getAllReplicaIdLists() {
        ArrayList<String> allReplicaIdList = new ArrayList<>();
        allReplicaIdList.addAll(ClientProperties.regularReplicaIdList);
        allReplicaIdList.addAll(ClientProperties.citizenReplicaIdList);
        return allReplicaIdList;
    }

    public static void print(String msg) {
        System.out.println("[o] " + msg);
    }

    public static void printError(String msg) {
        System.out.println("    [x] " + msg);
    }
}
