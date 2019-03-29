package hds.client.helpers;

public class ClientProperties {

    private static String port;

    private ClientProperties() {}

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        if (ClientProperties.port != null) { throw new RuntimeException("port is 'final'"); }
        ClientProperties.port = port;
    }
}
