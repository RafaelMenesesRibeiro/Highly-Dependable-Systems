package client.domain;

public class Client {
    private String name;

    public Client(String name) {
        this.name = name;
        System.out.println(String.format("Created client with name=%s", name));
    }
}
