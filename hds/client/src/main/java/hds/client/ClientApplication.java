package hds.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.domain.GetStateOfGoodCallable;
import hds.client.domain.IntentionToSellCallable;
import hds.client.helpers.ClientProperties;
import hds.security.CryptoUtils;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SaleCertificateResponse;
import hds.security.msgtypes.SaleRequestMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.security.SignatureException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static hds.client.helpers.ClientProperties.*;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.newUUIDString;
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.*;

@SpringBootApplication
public class ClientApplication {
    private static Scanner inputScanner = new Scanner(System.in);
    private static final AtomicInteger writeCounter = new AtomicInteger(0);

    /***********************************************************
     *
     * CLIENT COMMAND LINE INTERFACE AND SERVER INITIATION
     *
     ***********************************************************/

    public static void main(String[] args) {
        String portId = args[0];
        String maxPortId = args[1];
        int maxServerPort = 9000;
        try {
            maxServerPort = Integer.parseInt(args[2]);
        }
        catch (Exception ex) {
            Logger logger = Logger.getAnonymousLogger();
            logger.warning("Exiting:\n" + ex.getMessage());
            System.exit(-1);
        }
        ClientProperties.setPort(portId);
        ClientProperties.setMaxPortId(maxPortId);
        ClientProperties.initializeNotaryReplicasPortsList(maxServerPort);
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
     * GET STATE OF GOOD RELATED METHODS
     *
     ***********************************************************/

    private static void getStateOfGood() {
        final List<String> replicasList = ClientProperties.getNotaryReplicas();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);
        final String goodId = requestGoodId();

        List<Callable<BasicMessage>> callableList = new ArrayList<>();
        for (String replicaId : replicasList) {
            callableList.add(new GetStateOfGoodCallable(replicaId, goodId));
        }
        for (Callable<BasicMessage> callable : callableList) {
            completionService.submit(callable);
        }
        processCompletionService(replicasList.size(), completionService);
        executorService.shutdown();
    }

    private static void processCompletionService(int replicasCount,
                                                 ExecutorCompletionService<BasicMessage> completionService) {

        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                BasicMessage resultContent = futureResult.get();
                if (processResponse(resultContent)) break;
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                printError(cause.getMessage());
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            }
        }
    }

    /***********************************************************
     *
     * INTENTION TO SELL RELATED METHODS
     *
     ***********************************************************/

    private static void intentionToSell() {
        final List<String> replicasList = ClientProperties.getNotaryReplicas();
        final List<Callable<BasicMessage>> callableList = new ArrayList<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final String goodId = requestGoodId();
        final String requestId = newUUIDString();
        final long timestamp = generateTimestamp();

        for (String replicaId : replicasList) {
            callableList.add(new IntentionToSellCallable(
                    timestamp, requestId, replicaId, goodId, writeCounter.incrementAndGet(), Boolean.TRUE)
            );
        }

        List<Future<BasicMessage>> futuresList = new ArrayList<>();
        try {
            futuresList = executorService.invokeAll(callableList);
        } catch (InterruptedException ie) {
            printError(ie.getMessage());
        }

        processFuturesList(futuresList);

        executorService.shutdown();
    }

    private static void processFuturesList(List<Future<BasicMessage>> futuresList) {
        for (Future<BasicMessage> future : futuresList) {
            try {
                BasicMessage resultContent = future.get();
                processResponse(resultContent);
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof SocketTimeoutException) {
                    printError("Target node did not respond within expected limits. Try again at your discretion...");
                } else {
                    printError(cause.getMessage());
                }
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
            // BasicMessage responseMessage = getResponseMessage(connection, Expect.SALE_CERT_RESPONSE);
            JSONArray jsonArray = (JSONArray) getResponseMessage(connection, Expect.SALE_CERT_RESPONSES);

            if (jsonArray == null) {
                printError("Failed to deserialize json array (null) on buy goods with sale_cert_responses");
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject responseEntityObject = (JSONObject) jsonArray.get(i);
                JSONObject basicMessageObject = responseEntityObject.getJSONObject("body");

                BasicMessage basicMessage = null;
                ObjectMapper objectMapper = new ObjectMapper();

                if (basicMessageObject.has("reason")) {
                    objectMapper.readValue(basicMessageObject.toString(), ErrorResponse.class);
                } else {
                    objectMapper.readValue(basicMessageObject.toString(), SaleCertificateResponse.class);
                }
                processResponse(basicMessage);
            }

        } catch (SocketTimeoutException ste) {
            printError("Target node did not respond within expected limits. Try again at your discretion...");
        } catch (SignatureException | JSONException | IOException exc) {
            printError(exc.getMessage());
        }
    }

    private static SaleRequestMessage newSaleRequestMessage() {
        String to = requestSellerId();
        String goodId = requestGoodId();
        Boolean onSale = Boolean.FALSE;
        int logicalTimestamp = writeCounter.incrementAndGet();

        try {
            byte[] writeOnGoodsSignature = newWriteOnGoodsDataSignature(goodId, onSale, getPort(), logicalTimestamp);
            byte[] writeOnOwnershipsSignature = newWriteOnOwnershipsDataSignature(goodId, getPort(), logicalTimestamp);
            return new SaleRequestMessage(
                    generateTimestamp(),
                    newUUIDString(),
                    "buyGood",
                    ClientProperties.getPort(), // from
                    to,
                    "",
                    goodId,
                    ClientProperties.getPort(), // buyer
                    to, // seller
                    logicalTimestamp,
                    onSale,
                    bytesToBase64String(writeOnGoodsSignature),
                    bytesToBase64String(writeOnOwnershipsSignature)
            );
        } catch (JSONException | SignatureException exc) {
            throw new RuntimeException(exc.getMessage());
        }

    }

    private static byte[] newWriteOnGoodsDataSignature(final String goodId,
                                                       final Boolean onSale,
                                                       final String writer,
                                                       final int logicalTimestamp) throws JSONException, SignatureException {

        byte[] rawData = newWriteOnGoodsData(goodId, onSale, writer, logicalTimestamp).toString().getBytes();
        return CryptoUtils.signData(getPrivateKey(), rawData);
    }

    private static byte[] newWriteOnOwnershipsDataSignature(final String goodId,
                                                   final String writer,
                                                   final int logicalTimestamp) throws JSONException, SignatureException {

        byte[] rawData = newWriteOnOwnershipsData(goodId, writer, logicalTimestamp).toString().getBytes();
        return CryptoUtils.signData(getPrivateKey(), rawData);
    }
    /***********************************************************
     *
     * HELPER METHODS WITH NO LOGICAL IMPORTANCE
     *
     ***********************************************************/

    private static boolean processResponse(BasicMessage responseMessage) {
        // TODO Improve return value. Use something other than true/false;
        String validationResult = isValidMessage(ClientProperties.getPort(), responseMessage);
        if (!"".equals(validationResult)) {
            printError(validationResult);
            return false;
        }
        print(responseMessage.toString());
        return true;
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
}
