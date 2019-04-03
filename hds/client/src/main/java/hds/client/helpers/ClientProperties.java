package hds.client.helpers;

public class ClientProperties {

    private static String portId;
    private static String maxPortId;
    public static final String HDS_NOTARY_HOST = "http://hds-notary-server-production.herokuapp.com/";
    private ClientProperties() {}

    public static String getPort() {
        return portId;
    }

    public static void setPort(String portId) {
        if (ClientProperties.portId != null) { throw new RuntimeException("port is 'final'"); }
        ClientProperties.portId = portId;
    }

    public static String getMaxPortId() {
        return maxPortId;
    }

    public static void setMaxPortId(String maxPortId) {
        if (ClientProperties.maxPortId != null) { throw new RuntimeException("maxPortId is 'final'"); }
        ClientProperties.maxPortId = maxPortId;
    }
}
