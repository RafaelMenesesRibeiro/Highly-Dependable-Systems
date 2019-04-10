package hds.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.helpers.ClientProperties;
import hds.security.msgtypes.SaleRequestMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.SocketTimeoutException;
import java.net.HttpURLConnection;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static hds.client.helpers.ClientProperties.*;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.ConvertUtils.objectToByteArray;
import static hds.security.CryptoUtils.generateUniqueRequestId;
import static hds.security.CryptoUtils.signData;
import static hds.security.ResourceManager.*;

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
        try {
            PrivateKey clientPrivateKey = getPrivateKeyFromResource(ClientProperties.getPort());
            SaleRequestMessage saleRequestMessage = newSaleRequestMessage();
            saleRequestMessage.setSignature(bytesToBase64String(signData(clientPrivateKey, objectToByteArray(saleRequestMessage))));
            HttpURLConnection connection = initiatePOSTConnection(String.format("http://localhost:%s/wantToBuy", saleRequestMessage.getTo()));
            sendPostRequest(connection, newJSONObject(saleRequestMessage));
            processResponse(connection, HDS_NOTARY_PORT);
        } catch (SocketTimeoutException exc) {
            printError("Target node did not respond within expected limits. Try again at your discretion...");
        } catch (Exception exc) {
            printError(exc.getMessage());
        }
    }

    private static JSONObject newJSONObject(Object object) throws JsonProcessingException, JSONException {
        ObjectMapper objectMapper = new ObjectMapper();
        return new JSONObject(objectMapper.writeValueAsString(object));
    }

    private static SaleRequestMessage newSaleRequestMessage() {
        String requestId = generateUniqueRequestId();
        String from = ClientProperties.getPort();
        String buyerId = from;
        String to = requestSellerId();
        String sellerId = to;
        String goodId = requestGoodId();
        return new SaleRequestMessage(requestId, "buyGood", from, to,"", goodId,buyerId, sellerId);
    }

    private static void intentionToSell() {
        String sellerId = ClientProperties.getPort();
        try {
            PrivateKey sellerPrivateKey = getPrivateKeyFromResource(sellerId);
            OwnerData payload = new OwnerData(sellerId, requestGoodId());
            SignedOwnerData signedOwnerData = new SignedOwnerData();
            String signedPayload = bytesToBase64String(signData(sellerPrivateKey, objectToByteArray(payload)));
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
