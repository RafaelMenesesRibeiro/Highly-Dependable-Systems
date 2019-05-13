package hds.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.domain.CallableManager;
import hds.client.domain.GetStateOfGoodCallable;
import hds.client.domain.IntentionToSellCallable;
import hds.client.domain.WriteBackCallable;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ClientSecurityManager;
import hds.client.helpers.ONRRMajorityVoting;
import hds.security.msgtypes.*;
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
import static hds.security.helpers.managers.ConnectionManager.*;
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
        ClientProperties.setMyClientPort(portId);
        ClientProperties.setMaxFailures(maxFailures);
        ClientProperties.initializeRegularReplicasIDList(regularReplicasNumber);
        ClientProperties.initializeCCReplicasIDList(ccReplicasNumber);
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
        final List<String> replicasList = ClientProperties.getRegularReplicaIdList();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);
        final String goodId = requestGoodId();

        int rid = readId.incrementAndGet();

        for (String replicaId : replicasList) {
            Callable<BasicMessage> job = new GetStateOfGoodCallable(replicaId, goodId, rid);
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
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
                if (!futureResult.isCancelled()) {
                    BasicMessage message = futureResult.get();
                    if (message == null) {
                        continue;
                    }
                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.isGoodStateReadAcknowledge(rid, message, readList);
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
                printError("No good state response was found...");
            } else {
                print(String.format("Highest good state: %s, Highest owner state: %s\n", highestQuartet.getValue1(), highestQuartet.getValue3()));
                getStateOfGoodWriteBack(rid, highestQuartet.getValue0(), highestQuartet.getValue2());
            }
        }
    }

    private static void getStateOfGoodWriteBack(int rid, GoodStateResponse highestGoodState, GoodStateResponse highestOwnershipState) {
        print("Initiating write back phase to all known replicas...");

        final List<String> replicasList = ClientProperties.getRegularReplicaIdList();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);

        long timestamp = generateTimestamp();
        String requestId = newUUIDString();

        for (String replicaId : replicasList) {
            Callable<BasicMessage> job =
                    new WriteBackCallable(timestamp, requestId, replicaId, rid, highestGoodState, highestOwnershipState);
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
        }

        processGetStateOfGOodResponses(rid, replicasList.size(), completionService);
        executorService.shutdown();
    }

    /***********************************************************
     *
     * INTENTION TO SELL RELATED METHODS
     *
     ***********************************************************/

    private static void intentionToSell() {
        final List<String> replicasList = ClientProperties.getRegularReplicaIdList();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);
        long wts = generateTimestamp();
        final String goodId = requestGoodId();

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

    private static void processIntentionToSellResponses(long wts,
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
                    ackCount += ONRRMajorityVoting.iwWriteAcknowledge(wts, message);
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
     *
     * BUY GOOD RELATED METHODS
     *
     ***********************************************************/

    private static void buyGood() {
        try {
            long wts = generateTimestamp();

            SaleRequestMessage message = (SaleRequestMessage)setMessageSignature(getMyPrivateKey(), newSaleRequestMessage(wts));
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

    private static void processBuyGoodResponses(long wts, JSONArray messageList) {
        int ackCount = 0;
        for (int i = 0; i < messageList.length(); i++) {
            try {
                if (messageList.isNull(i)) {
                    printError("ClientApplication#processBuyGoodResponses(long wts, JSONArray messageList) had null element");
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

                    ackCount += ONRRMajorityVoting.iwWriteAcknowledge(wts, message);
                }

            } catch (JSONException | JsonParseException | JsonMappingException exc) {
                // swallow
            } catch (IOException ioe) {
                // swallow
            }
        }
        ONRRMajorityVoting.assertOperationSuccess(ackCount, "buyGood");
    }

    private static SaleRequestMessage newSaleRequestMessage(long wts) {
        String to = requestSellerId();
        String goodId = requestGoodId();
        Boolean onSale = Boolean.FALSE;

        try {
            byte[] writeOnGoodsSignature = ClientSecurityManager.newWriteOnGoodsDataSignature(goodId, onSale, getMyClientPort(), wts);
            byte[] writeOnOwnershipsSignature = ClientSecurityManager.newWriteOnOwnershipsDataSignature(goodId, getMyClientPort(), wts);
            return new SaleRequestMessage(
                    generateTimestamp(),
                    newUUIDString(),
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
     *
     * HELPER METHODS WITH NO LOGICAL IMPORTANCE
     *
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
