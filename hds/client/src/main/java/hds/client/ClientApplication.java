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
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", portId));
        app.run(args);

        while(acceptingCommands) {

            System.out.print("[o] Press '1' to get state of good, '2' to buy a good, '3' to put good to sell, '4' to quit... \n");

            try {
                input = inputScanner.nextInt();
                switch (input) {
                    case 1:
                        getStateOfGood();
                        break;
                    case 2:
                        buyGood();
                        break;
                    case 3:
                        intentToSell();
                    case 4:
                        acceptingCommands = false;
                        break;
                    default:
                        break;
                }
            } catch (NoSuchElementException | IllegalStateException exc) {
                continue;
            }
        }

        acceptingCommands = true;
    }

    private static void getStateOfGood() {
        String goodId = scanString("Input the good id: ");
    }

    private static void buyGood() {
        String goodId = scanString("Input the good id: ");
        String sellerId = scanString("Input the seller id: ");
    }

    private static void intentToSell() {
        String goodId = scanString("Input the good id: ");
    }

    private static String scanString(String requestString) {
        System.out.print(String.format("[o] %s", requestString));

        try {
            return inputScanner.next();
        } catch (NoSuchElementException | IllegalStateException exc) {
            return null;
        }
    }
}
