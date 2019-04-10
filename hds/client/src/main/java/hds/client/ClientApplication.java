package hds.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.helpers.ClientProperties;
import hds.security.domain.OwnerData;
import hds.security.domain.SignedOwnerData;
import hds.security.domain.SignedTransactionData;
import hds.security.domain.TransactionData;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.SocketTimeoutException;
import java.net.HttpURLConnection;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static hds.client.helpers.ClientProperties.HDS_NOTARY_HOST;
import static hds.client.helpers.ClientProperties.HDS_NOTARY_PORT;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.*;

@SpringBootApplication
public class ClientApplication {
    private static Scanner inputScanner = new Scanner(System.in);

    public static void main(String[] args) {
        String portId = args[0];
        String maxPortId = args[1];
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", portId));
        app.run(args);

        while (true) {
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
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }

    private static void buyGood() {
        String buyerId = ClientProperties.getPort();
        String sellerId = requestSellerId();
        String sellerURL = String.format("http://localhost:%s/", sellerId);
        String goodId = requestGoodId();

        try {
            PrivateKey clientPrivateKey = getPrivateKeyFromResource(buyerId);
            TransactionData payload = new TransactionData(sellerId, buyerId, goodId);
            SignedTransactionData signedTransactionData = new SignedTransactionData();
            String signedPayload = bytesToBase64String(signData(clientPrivateKey, getByteArray(payload)));
            signedTransactionData.setPayload(payload);
            signedTransactionData.setBuyerSignature(signedPayload);
            signedTransactionData.setSellerSignature("");

            ObjectMapper objectMapper = new ObjectMapper();
            JSONObject requestData = new JSONObject(objectMapper.writeValueAsString(signedTransactionData));

            String requestUrl = String.format("%s%s", sellerURL, "wantToBuy");
            HttpURLConnection connection = initiatePOSTConnection(requestUrl);
            sendPostRequest(connection, requestData);

            processResponse(connection, HDS_NOTARY_PORT);

        } catch (SocketTimeoutException exc) {
            printError("Target node did not respond within expected limits. Try again at your discretion.");
        } catch (Exception exc) {
            printError(exc.getMessage());
        }
    }

    private static void intentionToSell() {
        String sellerId = ClientProperties.getPort();
        try {
            PrivateKey sellerPrivateKey = getPrivateKeyFromResource(sellerId);
            OwnerData payload = new OwnerData(sellerId, requestGoodId());
            SignedOwnerData signedOwnerData = new SignedOwnerData();
            String signedPayload = bytesToBase64String(signData(sellerPrivateKey, getByteArray(payload)));
            signedOwnerData.setPayload(payload);
            signedOwnerData.setSignature(signedPayload);

            ObjectMapper objectMapper = new ObjectMapper();
            JSONObject requestData = new JSONObject(objectMapper.writeValueAsString(signedOwnerData));

            String requestUrl = String.format("%s%s", HDS_NOTARY_HOST, "intentionToSell");
            HttpURLConnection connection = initiatePOSTConnection(requestUrl);
            sendPostRequest(connection, requestData);

            processResponse(connection, HDS_NOTARY_PORT);

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
            processResponse(connection, HDS_NOTARY_PORT);
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
