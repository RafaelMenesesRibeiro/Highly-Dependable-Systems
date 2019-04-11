package hds.client.helpers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import static hds.security.ResourceManager.getPrivateKeyFromResource;

public class ClientProperties {

    private static String portId;
    private static String maxPortId;
    private static PrivateKey privateKey;
    public static final String HDS_BASE_HOST = "http://localhost:";
    public static final String HDS_NOTARY_HOST = "http://localhost:8000/";
    public static final String HDS_NOTARY_PORT = "8000";
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
}
