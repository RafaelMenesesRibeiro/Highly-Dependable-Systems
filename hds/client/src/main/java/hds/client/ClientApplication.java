package hds.client;

import hds.client.helpers.ClientProperties;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static hds.client.helpers.ClientProperties.HDS_NOTARY_HOST;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.*;

@SpringBootApplication
public class ClientApplication {
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
            print("Press '1' to get state of good, '2' to buy a good, '3' to put good on sale, '4' to quit: ");

            int input;
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
        String buyerId = ClientProperties.getPort();
        String sellerId = String.format("http://localhost:%s/", requestSellerId());
        String goodId = requestGoodId();

        try {
            String requestUrl = String.format("%s%s", sellerId, "wantToBuy");
            HttpURLConnection connection = initiatePOSTConnection(requestUrl);
            PrivateKey clientPrivateKey = getPrivateKeyFromResource(buyerId);

            JSONObject payload = new JSONObject();
            payload.put("sellerID", sellerId);
            payload.put("buyerID", buyerId);
            payload.put("goodID", goodId);

            JSONObject request = new JSONObject();
            request.put("buyerSignature", signData(clientPrivateKey, getByteArray(payload)));
            request.put("sellerSignature", null);
            request.put("payload", payload);

            sendPostRequest(connection, request);
            processResponse(connection, sellerId);

        } catch (SocketTimeoutException exc) {
            printError("Target node did not respond within expected limits. Try again at your discretion.");
        } catch (Exception exc) {
            printError(exc.getMessage());
        }
    }

    private static  void intentionToSell() {
        String clientId = ClientProperties.getPort();
        try {
            String requestUrl = String.format("%s%s", HDS_NOTARY_HOST, "intentionToSell");
            HttpURLConnection connection = initiatePOSTConnection(requestUrl);
            PrivateKey clientPrivateKey = getPrivateKeyFromResource(clientId);

            JSONObject payload = new JSONObject();
            payload.put("sellerID", clientId);
            payload.put("goodID", requestGoodId());

            JSONObject request = new JSONObject();
            request.put("signature", signData(clientPrivateKey, getByteArray(payload)));
            request.put("payload", payload);

            sendPostRequest(connection, request);
            processResponse(connection, HDS_NOTARY_HOST);

        } catch (SocketTimeoutException exc) {
            printError("Target node did not respond within expected limits. Try again at your discretion.");
        } catch (Exception exc) {
            printError(exc.getMessage());
        }
    }

    private static void getStateOfGood() {
        try {
            String requestUrl = String.format("%s%s%s", HDS_NOTARY_HOST, "stateOfGood?goodID=", requestGoodId());
            HttpURLConnection connection = initiateGETConnection(requestUrl);
            processResponse(connection, HDS_NOTARY_HOST);
            printError("Could not verify Notary signature...");
        } catch (SocketTimeoutException exc) {
            printError("Target node did not respond within expected limits. Try again at your discretion.");
        } catch (Exception exc) {
            printError(exc.getMessage());
        }
    }

    private static String requestGoodId() {
        return scanString("Provide good identifier: ");
    }

    private static String requestSellerId() {
        return scanString("Provide the owner of the good you want to buy.");
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
