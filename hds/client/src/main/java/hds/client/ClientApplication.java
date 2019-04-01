package hds.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.helpers.ClientProperties;
import hds.security.domain.GoodState;
import hds.security.domain.SecureResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.*;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static hds.client.helpers.ConnectionManager.getSecureResponse;
import static hds.client.helpers.ConnectionManager.initiateGETConnection;
import static hds.security.SecurityManager.*;

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
            print("Press '1' to get state of good, '2' to buy a good, '3' to put good on sale, '4' to quit: ");

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
                    intentionToSell();
                case 4:
                    acceptingCommands = false;
                    break;
                default:
                    break;
            }
        }

        acceptingCommands = true;
    }

    private static void buyGood() {
        // TODO
    }


    private static  void intentionToSell() {
        // TODO
    }

    private static void getStateOfGood() {
        try {
            String requestUrl = String.format("%s%s%s", HDS_NOTARY_HOST, "stateOfGood?goodID=", requestGoodId());
            HttpURLConnection connection = initiateGETConnection(requestUrl);
            SecureResponse secureResponse = getSecureResponse(connection);
            PublicKey HDSPublicKey = getPublicKeyFromResource(HDS_NOTARY_HOST);

            if (verifySignature(HDSPublicKey, secureResponse.getSignature(), secureResponse.getPayload())) {
                switch (connection.getResponseCode()) {
                    case(HttpURLConnection.HTTP_OK):
                        print(((GoodState)secureResponse.getPayload()).toString());
                        break;
                    case(HttpURLConnection.HTTP_NOT_FOUND):
                        printError("Specified good does not exist in the notary system;");
                        break;
                    default:
                        printError("Notary suffered from an internal error, please try again later;");
                        break;
                }
            } else {
                printError("This message was not sent produced by Notary Server!;");
            }
        } catch (Exception exc) {
            printError(exc.getMessage());
        }
    }

    private static String requestGoodId() {
        return scanString("Provide good identifier: ");
    }

    private static String scanString(String requestString) {
        print(requestString);
        try {
            return inputScanner.next();
        } catch (NoSuchElementException | IllegalStateException exc) {
            return scanString(requestString);
        }
    }

    private static void print(String msg) {
        System.out.println("[o] " + msg);
    }

    private static void printError(String msg) {
        System.out.println("    [x] " + msg);
    }
}
