package server.domain;

public class Server {
    private String ip;
    private String port;

    public Server(String ip, String port) {
        this.ip = ip;
        this.port = port;
        System.out.println(String.format("Created server with ip=%s and port=%s", ip, port));
    }
}