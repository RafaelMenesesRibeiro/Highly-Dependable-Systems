package hds.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import hds.client.exceptions.ResponseMessageException;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ConnectionManager;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.OwnerDataMessage;
import hds.security.msgtypes.SaleRequestMessage;
import org.json.JSONException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static hds.client.helpers.ClientProperties.*;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.CryptoUtils.newUUIDString;
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.isValidMessage;
import static hds.security.SecurityManager.setMessageSignature;

@SpringBootApplication
public class ClientApplication {
    private static Scanner inputScanner = new Scanner(System.in);

    /***********************************************************
     *
     * CLIENT COMMAND LINE INTERFACE AND SERVER INITIATION
     *
     ***********************************************************/

    public static void main(String[] args) {
        String portId = args[0];
        String maxPortId = args[1];
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        runClientServer(args);
        runClientInterface();
    }

    private static void runClientServer(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", ClientProperties.getPort()));
        app.run(args);
    }

    private static void runClientInterface() {
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

    /***********************************************************
     *
     * BUY GOOD RELATED METHODS
     *
     ***********************************************************/

    private static void buyGood() {
        try {
            SaleRequestMessage message = (SaleRequestMessage)setMessageSignature(getPrivateKey(), newSaleRequestMessage());
            HttpURLConnection connection = initiatePOSTConnection(HDS_BASE_HOST + message.getTo() + "/wantToBuy");
            sendPostRequest(connection, newJSONObject(message));
            BasicMessage responseMessage = getResponseMessage(connection, Expect.SALE_CERT_RESPONSE);
            processResponse(responseMessage);
        } catch (SocketTimeoutException ste) {
            printError("Target node did not respond within expected limits. Try again at your discretion...");
        } catch (ResponseMessageException | SignatureException | JSONException | IOException exc) {
           printError(exc.getMessage());
        }
    }

    public static SaleRequestMessage newSaleRequestMessage() {
        String to = requestSellerId();
        String goodId = requestGoodId();
        String sellerId = to;
        return new SaleRequestMessage(
                generateTimestamp(),
                newUUIDString(),
                "buyGood",
                ClientProperties.getPort(),
                to,
                "",
                goodId,
                ClientProperties.getPort(),
                sellerId
            );
    }

    /***********************************************************
     *
     * INTENTION TO SELL RELATED METHODS
     *
     ***********************************************************/

    private static void intentionToSell() {
        try {
            OwnerDataMessage message = (OwnerDataMessage)setMessageSignature(getPrivateKey(), newOwnerDataMessage());
            HttpURLConnection connection = initiatePOSTConnection(HDS_NOTARY_HOST + "intentionToSell");
            sendPostRequest(connection, newJSONObject(message));
            BasicMessage responseMessage = getResponseMessage(connection, Expect.BASIC_MESSAGE);
            processResponse(responseMessage);
        } catch (SocketTimeoutException ste) {
            printError("Target node did not respond within expected limits. Try again at your discretion...");
        } catch (ResponseMessageException | SignatureException | JSONException | IOException exc) {
            printError(exc.getMessage());
        }
    }

    private static OwnerDataMessage newOwnerDataMessage() {
        String goodId = requestGoodId();
        return new OwnerDataMessage(
                generateTimestamp(), newUUIDString(),"intentionToSell", getPort(), HDS_NOTARY_PORT,"", goodId, getPort()
        );
    }

    /***********************************************************
     *
     * GET STATE OF GOOD RELATED METHODS
     *
     ***********************************************************/

    private static void getStateOfGood() {
        try {
            String requestUrl = HDS_NOTARY_HOST + "stateOfGood?goodID=" + requestGoodId();
            HttpURLConnection connection = initiateGETConnection(requestUrl);
            BasicMessage responseMessage = getResponseMessage(connection, Expect.GOOD_STATE_RESPONSE);
            processResponse(responseMessage);
        } catch (SocketTimeoutException ste) {
            printError("Target node did not respond within expected limits. Try again at your discretion...");
        } catch (ResponseMessageException | IOException exc) {
            printError(exc.getMessage());
        }
    }

    /***********************************************************
     *
     * HELPER METHODS WITH NO LOGICAL IMPORTANCE
     *
     ***********************************************************/

    private static void processResponse(BasicMessage responseMessage) {
        String validationResult = isValidMessage(ClientProperties.getPort(), responseMessage);
        if (!"".equals(validationResult)) {
            printError(validationResult);
        } else {
            print(responseMessage.toString());
        }
    }

    private static String scanString(String requestString) {
        print(requestString);
        try {
            return inputScanner.next();
        } catch (NoSuchElementException | IllegalStateException exc) {
            return scanString(requestString);
        }
    }

    private static String requestGoodId() {
        return scanString("Provide good identifier: ");
    }

    private static String requestSellerId() {
        return scanString("Provide the owner of the good you want to buy.");
    }

    private static void print(String msg) {
        System.out.println("[o] " + msg);
    }

    private static void printError(String msg) {
        System.out.println("    [x] " + msg);
    }
}
