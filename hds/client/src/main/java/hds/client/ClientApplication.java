package hds.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.domain.*;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ClientSecurityManager;
import hds.client.helpers.ONRRMajorityVoting;
import hds.security.msgtypes.*;
import org.javatuples.Pair;
import org.javatuples.Quartet;
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
import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.newUUIDString;
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.setMessageSignature;
import static hds.security.helpers.managers.ConnectionManager.*;

@SuppressWarnings("Duplicates")
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
        int regularReplicasNumber = 1;
        int ccReplicasNumber = 0;
        int maxFailures = 0;
        try {
            regularReplicasNumber = Integer.parseInt(args[1]);
            ccReplicasNumber = Integer.parseInt(args[2]);
            maxFailures = Integer.parseInt(args[3]);
        }
        catch (Exception ex) {
            Logger logger = Logger.getAnonymousLogger();
            logger.warning("Exiting:\n" + ex.getMessage());
            System.exit(-1);
        }

        ClientProperties.init(portId, maxFailures, regularReplicasNumber, ccReplicasNumber);

        runClientServer(args);
        runClientInterface();
    }

    private static void runClientServer(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", ClientProperties.getMyClientPort()));
        app.run(args);
    }

    private static void runClientInterface() {
        while (true) {
            print("\nPress '1' to get state of good, '2' to buy a good, '3' to put good on sale, '4' to quit: ");
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
     * GET WTS RELATED METHODS
     ***********************************************************/

    private static int readWts() {
        final List<String> replicasList = ClientProperties.getReplicasList();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);

        int rid = readId.incrementAndGet();

        for (String replicaId : replicasList) {
            Callable<BasicMessage> job = new ReadWtsCallable(replicaId, getMyClientPort(), rid);
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
        }

        int wts = processReadWtsResponses(rid, replicasList.size(), completionService);

        executorService.shutdown();

        print("My commit wts attempt: " + (wts + 1) + "\n");
        return wts + 1;
    }

    private static int processReadWtsResponses(final int rid,
                                                final int replicasCount,
                                                ExecutorCompletionService<BasicMessage> completionService) {

        int ackCount = 0;
        List<ReadWtsResponse> readList = new ArrayList<>();
        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                if (!futureResult.isCancelled()) {
                    BasicMessage message = futureResult.get();
                    if (message == null) {
                        continue;
                    }
                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.isReadWtsAcknowledge(rid, message, readList);
                }
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                printError(cause.getMessage());
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            }
        }
        if (ONRRMajorityVoting.assertOperationSuccess(ackCount, "readWts")) {
            Pair<ReadWtsResponse, Integer> highestPair = ONRRMajorityVoting.selectMostRecentWts(readList);
            if (highestPair == null) {
                printError("No wts responses were found...");
            } else {
                print(String.format("Highest wts: %s", highestPair.getValue1()));
                return highestPair.getValue1();
            }
        } else {
            printError("");
        }
        return -1;
    }

    /***********************************************************
     * GET STATE OF GOOD RELATED METHODS
     ***********************************************************/

    private static void getStateOfGood() {
        final List<String> replicasList = ClientProperties.getReplicasList();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);
        final String goodId = requestGoodId();

        int rid = readId.incrementAndGet();

        for (String replicaId : replicasList) {
            Callable<BasicMessage> job = new GetStateOfGoodCallable(replicaId, goodId, rid);
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
        }

        Quartet<GoodStateResponse, Boolean, GoodStateResponse, String> highestQuartet =
                processGetStateOfGoodResponses(rid, replicasList.size(), completionService);

        if (highestQuartet == null) {
            return;
        } else {
            getStateOfGoodWriteBack(rid, highestQuartet.getValue0(), highestQuartet.getValue2());
        }

        executorService.shutdown();
    }

    public static Quartet<GoodStateResponse, Boolean, GoodStateResponse, String> processGetStateOfGoodResponses(
            final int rid,
            final int replicasCount,
            ExecutorCompletionService<BasicMessage> completionService
    ) {

        int ackCount = 0;
        List<GoodStateResponse> readList = new ArrayList<>();
        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                if (!futureResult.isCancelled()) {
                    BasicMessage message = futureResult.get();
                    if (message == null) {
                        continue;
                    }
                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.isGetGoodStateAcknowledge(rid, message, readList);
                }
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                printError(cause.getMessage());
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            }
        }

        if (ONRRMajorityVoting.assertOperationSuccess(ackCount, "getStateOfGood")) {
            Quartet<GoodStateResponse, Boolean, GoodStateResponse, String> highestQuartet =
                    ONRRMajorityVoting.selectMostRecentGoodState(readList);
            if (highestQuartet == null) {
                printError("No good state responses were found...");
            } else {
                print(String.format("\nHighest good state: %s, Highest owner state: %s\n", highestQuartet.getValue1(), highestQuartet.getValue3()));
                return highestQuartet;
            }
        }
        return null;
    }

    private static void getStateOfGoodWriteBack(int rid, GoodStateResponse highestGoodState, GoodStateResponse highestOwnershipState) {
        print("Initiating get state of good write back phase to all known replicas...");

        final List<String> replicasList = ClientProperties.getReplicasList();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);

        long timestamp = generateTimestamp();
        String requestId = newUUIDString();
        for (String replicaId : replicasList) {
            Callable<BasicMessage> job =
                    new WriteBackCallable(timestamp, requestId, replicaId, rid, highestGoodState, highestOwnershipState);
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
        }

        processGetStateOfGOodWriteBackResponses(rid, replicasList.size(), completionService);
        executorService.shutdown();
    }

    private static void processGetStateOfGOodWriteBackResponses(final int rid,
                                                                final int replicasCount,
                                                                ExecutorCompletionService<BasicMessage> completionService) {

        int ackCount = 0;
        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                if (!futureResult.isCancelled()) {
                    BasicMessage message = futureResult.get();
                    if (message == null) {
                        continue;
                    }
                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.isGetGoodStateWriteBackAcknowledge(rid, message);
                }
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                printError(cause.getMessage());
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            }
        }
        if (ONRRMajorityVoting.assertOperationSuccess(ackCount, "getStateOfGoodWriteBack")) {
            print("Get state of good operation with rid: " + rid + " had a successful right back phase...");
        }
    }

    /***********************************************************
     * INTENTION TO SELL RELATED METHODS
     ***********************************************************/

    private static void intentionToSell() {
        final List<String> replicasList = ClientProperties.getReplicasList();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);
        final String goodId = requestGoodId();

        int wts = readWts();

        if (wts == 0) {
            print("Invalid wts, can't proceed with intention to sell operation...");
            return;
        }

        long timestamp = generateTimestamp();
        String requestId = newUUIDString();
        for (String replicaId : replicasList) {
            Callable<BasicMessage> job =
                    new IntentionToSellCallable(timestamp, requestId, replicaId, goodId, wts, Boolean.TRUE);
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
        }

        processIntentionToSellResponses(wts, replicasList.size(), completionService);
        executorService.shutdown();
    }

    private static void processIntentionToSellResponses(int wts,
                                                        final int replicasCount,
                                                        ExecutorCompletionService<BasicMessage> completionService) {

        int ackCount = 0;
        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                    if (!futureResult.isCancelled()) {
                    BasicMessage message = futureResult.get();
                    if (message == null) {
                        continue;
                    }
                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.isIntentionToSellAcknowledge(wts, message);
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
        }
        ONRRMajorityVoting.assertOperationSuccess(ackCount,"intentionToSell");
    }

    /***********************************************************
     * BUY GOOD RELATED METHODS
     ***********************************************************/

    private static void buyGood() {
        try {
            String to = requestSellerId();
            String goodId = requestGoodId();

            int wts = readWts();
            if (wts == 0) {
                print("Invalid wts, can't proceed with buy good operation...");
                return;
            }

            long timestamp = generateTimestamp();
            String requestId = newUUIDString();
            SaleRequestMessage message =
                    (SaleRequestMessage)setMessageSignature(getMyPrivateKey(), newSaleRequestMessage(timestamp, requestId, wts, to, goodId));
            HttpURLConnection connection = initiatePOSTConnection(HDS_BASE_HOST + message.getTo() + "/wantToBuy");
            sendPostRequest(connection, newJSONObject(message));
            JSONArray jsonArray = (JSONArray) getResponseMessage(connection, Expect.SALE_CERT_RESPONSES);

            if (jsonArray == null) {
                printError("Failed to deserialize json array (null) on buyGood with SALE_CERT_RESPONSES");
            } else {
                processBuyGoodResponses(wts, jsonArray);
            }
        } catch (SignatureException | JSONException | IOException exc) {
            printError(exc.getMessage());
        }
    }

    private static void processBuyGoodResponses(int wts, JSONArray messageList) {
        int ackCount = 0;
        for (int i = 0; i < messageList.length(); i++) {
            try {
                if (messageList.isNull(i)) {
                    printError("Seller (mediator) claims a replica timed out. No information regarding the replicaId...");
                } else {
                    BasicMessage message;
                    ObjectMapper objectMapper = new ObjectMapper();
                    JSONObject messageObject = (JSONObject) messageList.get(i);

                    if (messageObject.has("reason")) {
                        message = objectMapper.readValue(messageObject.toString(), ErrorResponse.class);
                    } else {
                        message = objectMapper.readValue(messageObject.toString(), SaleCertificateResponse.class);
                    }

                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        continue;
                    }

                    ackCount += ONRRMajorityVoting.isBuyGoodAcknowledge(wts, message);
                }

            } catch (JSONException | JsonParseException | JsonMappingException exc) {
                // swallow
            } catch (IOException ioe) {
                // swallow
            }
        }
        ONRRMajorityVoting.assertOperationSuccess(ackCount, "buyGood");
    }

    private static SaleRequestMessage newSaleRequestMessage(long timestamp, String requestId, int wts, String to, String goodId) {
        Boolean onSale = Boolean.FALSE;
        try {
            byte[] writeOnGoodsSignature = ClientSecurityManager.newWriteOnGoodsDataSignature(goodId, onSale, getMyClientPort(), wts);
            byte[] writeOnOwnershipsSignature = ClientSecurityManager.newWriteOnOwnershipsDataSignature(goodId, getMyClientPort(), wts);
            return new SaleRequestMessage(
                    timestamp,
                    requestId,
                    "buyGood",
                    ClientProperties.getMyClientPort(), // from
                    to,
                    "",
                    goodId,
                    ClientProperties.getMyClientPort(), // buyer
                    to, // seller
                    wts,
                    onSale,
                    bytesToBase64String(writeOnGoodsSignature),
                    bytesToBase64String(writeOnOwnershipsSignature)
            );
        } catch (JSONException | SignatureException exc) {
            throw new RuntimeException(exc.getMessage());
        }

    }

    /***********************************************************
     * HELPER METHODS WITH NO LOGICAL IMPORTANCE
     ***********************************************************/

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
