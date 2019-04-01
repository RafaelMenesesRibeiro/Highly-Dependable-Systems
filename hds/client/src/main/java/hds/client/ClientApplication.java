package hds.client;

import hds.client.helpers.ClientProperties;
import jdk.internal.util.xml.impl.Input;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.NoSuchAlgorithmException;
import java.util.*;

@SpringBootApplication
public class ClientApplication {
    private static int input;
    private static boolean acceptingCommands = true;
    private static Scanner inputScanner = new Scanner(System.in);

    public static void main(String[] args) {
        String portId = args[0];
        String maxPortId = args[1];
        System.out.println(portId);
        System.out.println(maxPortId);
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", portId));
        app.run(args);

        while(acceptingCommands) {

            System.out.print("[o] Press '1' to get state of good, '2' to buy a good, '3' to quit... \n");

            try {
                input = inputScanner.nextInt();
            } catch (NoSuchElementException | IllegalStateException exc) {
                continue;
            }

            switch (input) {
                case 1:
                    getStateOfGood();
                case 2:
                    buyGood();
                case 3:
                    acceptingCommands = false;
                default:
                    break;
            }
        }

        acceptingCommands = true;
    }

    private static void getStateOfGood() {
    }

    private static void buyGood() {
    }
}
