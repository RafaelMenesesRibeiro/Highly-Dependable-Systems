package hds.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.domain.GetStateOfGoodCallable;
import hds.client.domain.IntentionToSellCallable;
import hds.client.helpers.ClientProperties;
import hds.security.CryptoUtils;
import hds.security.DateUtils;
import hds.security.msgtypes.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
    private static final AtomicInteger readId = new AtomicInteger(0);

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

        int rid = readId.incrementAndGet();

        List<Callable<BasicMessage>> callableList = new ArrayList<>();
        for (String replicaId : replicasList) {
            callableList.add(new GetStateOfGoodCallable(replicaId, goodId, rid));
        }
        for (Callable<BasicMessage> callable : callableList) {
            completionService.submit(callable);
        }
        processGetStateOfGOodResponses(rid, replicasList.size(), completionService);
        executorService.shutdown();
    }

    private static void processGetStateOfGOodResponses(final int rid,
                                                       final int replicasCount,
                                                       ExecutorCompletionService<BasicMessage> completionService) {

        int ackCount = 0;
        List<GoodStateResponse> readList = new ArrayList<>();
        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                BasicMessage result = futureResult.get();

                if (!isFreshAndAuthentic(result)) {
                    printError("Ignoring invalid message...");
                    continue;
                }

                ackCount += isGoodStateResponseAcknowledge(rid, result, readList);
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                printError(cause.getMessage());
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            }
        }
        if (assertOperationSuccess(ackCount, "getStateOfGood")) {
            print(highestValue(readList).toString());
        }

    }

    private static int isGoodStateResponseAcknowledge(int rid, BasicMessage message, List<GoodStateResponse> readList) {
        if (message == null) {
            return 0;
        } else if (message instanceof GoodStateResponse) {
            GoodStateResponse goodStateResponse = (GoodStateResponse) message;

            if (rid != goodStateResponse.getRid()) {
                return 0;
            }

            if (!verifyWriteOnGoodsDataResponseSignature(
                    goodStateResponse.getGoodID(),
                    goodStateResponse.isOnSale(),
                    goodStateResponse.getWriterID(),
                    goodStateResponse.getWts(),
                    goodStateResponse.getWriteOperationSignature()
            )) {
                return 0;
            }

            readList.add(goodStateResponse);
            return 1;
        }
        printError(message.toString());
        return 0;
    }

    private static BasicMessage highestValue(List<GoodStateResponse> readList) {
        GoodStateResponse highest = null;
        for (GoodStateResponse message : readList) {
            if (highest == null) {
                highest = message;
            } else if (DateUtils.isOneTimestampAfterAnother(message.getWts(), highest.getWts())) {
                highest = message;
            }
        }
        return highest;
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
        // Store the write operation timestamp of this operation in order to validate incoming responses
        long wts = generateTimestamp();
        // Create a list of callable, so that servers can be called in parallel by this client
        for (String replicaId : replicasList) {
            callableList.add(new IntentionToSellCallable(
                    generateTimestamp(), newUUIDString(), replicaId, requestGoodId(), wts, Boolean.TRUE)
            );
        }
        // Initiate all tasks and wait for all of them to finish TODO implement timeouts
        List<Future<BasicMessage>> futuresList = new ArrayList<>();
        try {
            futuresList = executorService.invokeAll(callableList,20, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            printError(ie.getMessage());
        }
        // Validate responses for freshness, authenticity and see if they are a response for this request
        processIntentionToSellResponses(wts, futuresList);
        // End operation
        executorService.shutdown();
    }

    private static void processIntentionToSellResponses(long wts, List<Future<BasicMessage>> futuresList) {
        int ackCount = 0;
        for (Future<BasicMessage> future : futuresList) {
            if (!future.isCancelled()) {
                BasicMessage resultContent = null;
                try {
                    resultContent = future.get();
                    if (!isFreshAndAuthentic(resultContent)) {
                        printError("Ignoring invalid message...");
                        continue;
                    }
                } catch (InterruptedException ie) {
                    printError(ie.getMessage());
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    printError(cause.getMessage());
                    if (cause instanceof SocketTimeoutException) {
                        printError("Target node did not respond within expected limits. Try again at your discretion...");
                    }
                }
                ackCount += isWriteResponseAcknowledge(wts, resultContent);
            }
        }
        assertOperationSuccess(ackCount, "intentionToSell");
    }

    private static int isWriteResponseAcknowledge(long wts, BasicMessage message) {
        if (message == null) {
            return 0;
        } else if (message instanceof WriteResponse) {
            if (((WriteResponse) message).getWts() == wts) {
                return 1;
            } else {
                printError("Response contained wts different than the one that was sent on request");
                return 0;
            }
        }
        printError(message.toString());
        return 0;
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

                System.out.println(basicMessageObject.toString(4));

                BasicMessage basicMessage = null;
                ObjectMapper objectMapper = new ObjectMapper();

                if (basicMessageObject.has("reason")) {
                    basicMessage = objectMapper.readValue(basicMessageObject.toString(), ErrorResponse.class);
                } else {
                    basicMessage = objectMapper.readValue(basicMessageObject.toString(), SaleCertificateResponse.class);
                }

                isFreshAndAuthentic(basicMessage);
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
        int logicalTimestamp = readId.incrementAndGet();

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

    private static byte[] newWriteOnOwnershipsDataSignature(final String goodId,final String writerID,
                                                            final int logicalTimestamp) throws JSONException, SignatureException {

        byte[] rawData = newWriteOnOwnershipData(goodId, writerID, logicalTimestamp).toString().getBytes();
        return CryptoUtils.signData(getPrivateKey(), rawData);
    }
    /***********************************************************
     *
     * HELPER METHODS WITH NO LOGICAL IMPORTANCE
     *
     ***********************************************************/

    private static boolean isFreshAndAuthentic(BasicMessage responseMessage) {
        if (responseMessage == null) {
            return false;
        }
        // Verify freshness and authenticity using isValidMessage
        String validityString = isValidMessage(responseMessage);
        if (!"".equals(validityString)) {
            // Non-empty string means something went wrong during validation. Message isn't fresh or isn't properly signed
            printError(validityString);
            return false;
        }
        else {
            // Everything is has expected
            print(responseMessage.toString());
            return true;
        }
    }

    private static boolean assertOperationSuccess(int ackCount, String operation) {
        if (ackCount > ClientProperties.getMajorityThreshold()) {
            print(operation + " operation finished with majority quorum!");
            return true;
        } else {
            print(operation + " operation failed... Not enough votes.");
            return false;
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
}
