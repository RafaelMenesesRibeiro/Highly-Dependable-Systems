package hds.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.domain.GetStateOfGoodCallable;
import hds.client.domain.IntentionToSellCallable;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ClientSecurityManager;
import hds.client.helpers.ONRRMajorityVoting;
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
                BasicMessage message = futureResult.get();

                if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                    continue;
                }

                ackCount += ONRRMajorityVoting.isGoodStateReadAcknowledge(rid, message, readList);
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                printError(cause.getMessage());
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            }
        }
        if (ONRRMajorityVoting.assertOperationSuccess(ackCount, "getStateOfGood")) {
            print(ONRRMajorityVoting.selectMostRecentGoodState(readList).toString());
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
                try {
                    BasicMessage message = future.get();
                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.iwWriteAcknowledge(wts, message);
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
        }
        ONRRMajorityVoting.assertOperationSuccess(ackCount, "intentionToSell");
    }

    /***********************************************************
     *
     * BUY GOOD RELATED METHODS
     *
     ***********************************************************/

    private static void buyGood() {
        try {
            long wts = generateTimestamp();

            SaleRequestMessage message = (SaleRequestMessage)setMessageSignature(getPrivateKey(), newSaleRequestMessage(wts));
            HttpURLConnection connection = initiatePOSTConnection(HDS_BASE_HOST + message.getTo() + "/wantToBuy");
            sendPostRequest(connection, newJSONObject(message));
            JSONArray jsonArray = (JSONArray) getResponseMessage(connection, Expect.SALE_CERT_RESPONSES);

            if (jsonArray == null) {
                printError("Failed to deserialize json array (null) on buyGood with SALE_CERT_RESPONSES");
            }

            processBuyGoodResponses(wts, jsonArray);

        } catch (SocketTimeoutException ste) {
            printError("Target node did not respond within expected limits. Try again at your discretion...");
        } catch (SignatureException | JSONException | IOException exc) {
            printError(exc.getMessage());
        }
    }

    private static void processBuyGoodResponses(long wts, JSONArray messageList) {
        int ackCount = 0;
        for (int i = 0; i < messageList.length(); i++) {
            try {
                BasicMessage message;
                JSONObject basicMessageObject = (JSONObject) messageList.get(i);

                ObjectMapper objectMapper = new ObjectMapper();
                if (basicMessageObject.has("reason")) {
                    message = objectMapper.readValue(basicMessageObject.toString(), ErrorResponse.class);
                } else {
                    message = objectMapper.readValue(basicMessageObject.toString(), SaleCertificateResponse.class);
                }

                if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                    continue;
                }

                ackCount += ONRRMajorityVoting.iwWriteAcknowledge(wts, message);
                
            } catch (Exception exc) {
                continue;
            }
        }
        ONRRMajorityVoting.assertOperationSuccess(ackCount, "buyGood");
    }

    private static SaleRequestMessage newSaleRequestMessage(long wts) {
        String to = requestSellerId();
        String goodId = requestGoodId();
        Boolean onSale = Boolean.FALSE;

        try {
            byte[] writeOnGoodsSignature = ClientSecurityManager.newWriteOnGoodsDataSignature(goodId, onSale, getPort(), wts);
            byte[] writeOnOwnershipsSignature = ClientSecurityManager.newWriteOnOwnershipsDataSignature(goodId, getPort(), wts);
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
