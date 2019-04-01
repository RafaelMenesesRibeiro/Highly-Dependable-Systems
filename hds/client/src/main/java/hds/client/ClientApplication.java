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
        System.out.println(portId);
        System.out.println(maxPortId);
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", portId));
        app.run(args);

        while(acceptingCommands) {

            print("Press '1' to get state of good, '2' to buy a good, '3' to quit...");

            try {
                input = inputScanner.nextInt();
            } catch (NoSuchElementException | IllegalStateException exc) {
                continue;
            }

            switch (input) {
                case 1:
                    getStateOfGood();
                    break;
                case 2:
                    buyGood();
                    break;
                case 3:
                    acceptingCommands = false;
                    break;
                default:
                    break;
            }
        }

        acceptingCommands = true;
    }


    private static void getStateOfGood() {
        try {
            StringBuilder requestUrl = new StringBuilder(HDS_NOTARY_HOST + "stateOfGood?goodID=");

            requestUrl.append(scanString("Provide good identifier..."));

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
    }

    private static void print(String msg) {
        System.out.println("[o] " + msg);
    }

    private static void printError(String msg) {
        System.out.println("    [x] " + msg);
    }
}
