package hds.client;

import hds.client.helpers.ClientProperties;
import jdk.internal.util.xml.impl.Input;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@SpringBootApplication
public class ClientApplication {
    private static int input;
    private static boolean acceptingCommands = true;
    private static final String HDS_NOTARY_HOST = "http://localhost:8000/";
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

            System.out.print("[o] Press '1' to get state of good, '2' to buy a good, '3' to put good on sale, '4' to quit... \n");

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
        String goodId = scanString("Provide good identifier: ");
        try {
            String requestUrl = String.format("%s%s%s", HDS_NOTARY_HOST, "stateOfGood?goodID=", goodId);

            URL url = new URL(requestUrl.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // ...
            }

        } catch (MalformedURLException exc) {
            printError("MalformedURLException: could not resolve notary server host;");
        } catch (ProtocolException exc) {
            printError("ProtocolException: invalid request method;");
        } catch (IOException exc) {
            printError("IOException: " + exc.getMessage() + ";");
        } catch (IllegalStateException exc) {
            printError("IllegalStateException: connection was unintentionally opened twice;");
        }
    }

    private static void buyGood() {
        String goodId = scanString("Provide good identifier: ");
        String sellerId = scanString("Provide seller identifier: ");
    }

    private static void intentToSell() {
        String goodId = scanString("Provide good identifier: ");
    }

    private static String scanString(String requestString) {
        print(requestString);

        try {
            return inputScanner.next();
        } catch (NoSuchElementException | IllegalStateException exc) {
            return null;
        }
    }

    private static void print(String msg) {
        System.out.println("[o] " + msg);
    }

    private static void printError(String msg) {
        System.out.println("    [x] " + msg);
    }
}
