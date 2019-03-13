import server.domain.Server;

public class App {
    public static void main(String[] args) {
		System.out.println("Hello world");
    	Server server = new Server("localhost", "8014");
    }
}