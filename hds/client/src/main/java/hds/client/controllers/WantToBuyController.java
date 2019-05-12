package hds.client.controllers;

import hds.client.domain.CallableManager;
import hds.client.domain.RequestChallengeCallable;
import hds.client.domain.TransferGoodCallable;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ONRRMajorityVoting;
import hds.security.ChallengeSolver;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ChallengeRequestResponse;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SaleRequestMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.SocketTimeoutException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static hds.client.helpers.ClientProperties.*;
import static hds.client.helpers.ClientSecurityManager.isMessageFreshAndAuthentic;
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.setMessageSignature;

@RestController
public class WantToBuyController {

    private static final String OPERATION = "wantToBuy";

    @PostMapping(value = "/wantToBuy")
    public ResponseEntity<List<BasicMessage>>wantToBuy(@RequestBody SaleRequestMessage requestMessage) {
        if (isMessageFreshAndAuthentic(requestMessage)) {
            return initiateTwoPhaseProtocol(requestMessage);
        }
        return newSignedError(requestMessage, "Seller rejects message, it's either not fresh or not properly signed");
    }

    private ResponseEntity<List<BasicMessage>> initiateTwoPhaseProtocol(SaleRequestMessage requestMessage) {
        // Obtain replicas known to this server
        final List<String> replicasList = ClientProperties.getRegularReplicaIdList();
        // For each of them, get a challenge that will allow transfer good to be effectuated;
        final Map<String, ChallengeRequestResponse>challengesList = getChallenges(replicasList, requestMessage.getRequestID());
        // If majority of replicas replied with a challenge, solve all challenges and proceed
        if (challengesList == null) {
            return newSignedError(requestMessage, "Not enough challenges were gathered");
        } else {
            List<BasicMessage> transferResponses = tryEffectuateTransfer(requestMessage, challengesList);
            return new ResponseEntity<>(transferResponses, HttpStatus.MULTIPLE_CHOICES);
        }
    }

    /***********************************************************
     * FIRST PHASE OF TRANSFER GOOD
     ***********************************************************/

    private Map<String, ChallengeRequestResponse> getChallenges(final List<String> replicasList, final String requestId) {
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);

        final long timestamp = generateTimestamp();

        for (String replicaId : replicasList) {
            Callable<BasicMessage> job = new RequestChallengeCallable(timestamp, replicaId, requestId);
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
        }

        Map<String, ChallengeRequestResponse> replicaIdUnsolvedChallengeMap =
                processChallengeRequestResponses(replicasList.size(), completionService);

        executorService.shutdown();
        return replicaIdUnsolvedChallengeMap;
    }

    private Map<String, ChallengeRequestResponse> processChallengeRequestResponses(final int replicasCount,
                                                                                   ExecutorCompletionService<BasicMessage> completionService) {

        Map<String, ChallengeRequestResponse> replicaIdUnsolvedChallengeMap = new HashMap<>();
        int ackCount = 0;
        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                if  (!futureResult.isCancelled()) {
                    BasicMessage message = futureResult.get();
                    if (message == null) {
                        continue;
                    }
                    if (!isMessageFreshAndAuthentic(message)) {
                        printError("Ignoring invalid message...");
                        continue;
                    }
                    if (message instanceof ChallengeRequestResponse) {
                        replicaIdUnsolvedChallengeMap.put(message.getFrom(), (ChallengeRequestResponse) message);
                        ackCount++;
                    }
                }
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                printError(cause.getMessage());
                if (cause instanceof SocketTimeoutException) {
                    printError("A node did not respond within expected limits...");
                } else if (cause instanceof SignatureException) {
                    printError("Seller could not sign a message to be sent to at least one of the replicas...");
                }
            }
        }
        if (ONRRMajorityVoting.assertOperationSuccess(ackCount, "getChallenges")) {
            return replicaIdUnsolvedChallengeMap;
        } else {
            return null;
        }
    }

    /***********************************************************
     * SECOND PHASE OF TRANSFER GOOD
     ***********************************************************/

    private List<BasicMessage> tryEffectuateTransfer(final SaleRequestMessage requestMessage,
                                                     final Map<String, ChallengeRequestResponse> challengesMap) {

        Map<String, String> replicaIdChallengeSolutionsMap = solveChallenges(challengesMap);

        final ExecutorService executorService = Executors.newFixedThreadPool(replicaIdChallengeSolutionsMap.size());
        final ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);
        final long timestamp = generateTimestamp();

        for (Map.Entry<String, String> entry : replicaIdChallengeSolutionsMap.entrySet()) {
            Callable<BasicMessage> job = new TransferGoodCallable(timestamp, entry.getKey(), requestMessage, entry.getValue());
            completionService.submit(new CallableManager(job,10, TimeUnit.SECONDS));
        }

        List<BasicMessage> responses =
                processTransferGoodResponses(requestMessage.getWts(), replicaIdChallengeSolutionsMap.size(), completionService);

        executorService.shutdown();
        return responses;
    }

    private Map<String, String> solveChallenges(final Map<String, ChallengeRequestResponse> challengesMap) {
        Map<String, String> replicaIdChallengeSolutionsMap = new HashMap<>();
        for (Map.Entry<String, ChallengeRequestResponse> entry : challengesMap.entrySet()) {
            System.out.println("Solving challenge for replica: " + entry.getKey());
            ChallengeRequestResponse challenge = entry.getValue();
            String solution = ChallengeSolver.solveChallenge(
                    challenge.getHashedOriginalString(),
                    challenge.getOriginalStringSize(),
                    challenge.getAlphabet()
            );
            System.out.println("Found possible solution: " + solution + ", for challenge of replica: " + entry.getKey());
            replicaIdChallengeSolutionsMap.put(entry.getKey(), solution);
        }
        return replicaIdChallengeSolutionsMap;
    }

    private List<BasicMessage> processTransferGoodResponses(long wts,
                                                            final int replicasCount,
                                                            ExecutorCompletionService<BasicMessage> completionService) {

        List<BasicMessage> messagesList = new ArrayList<>();
        int ackCount = 0;
        for (int i = 0; i < replicasCount; i++) {
            try {
                Future<BasicMessage> futureResult = completionService.take();
                if  (!futureResult.isCancelled()) {
                    BasicMessage message = futureResult.get();
                    messagesList.add(message); // TODO Be careful with this one when parsing on client for JSON
                    if (message == null) {
                        continue;
                    }
                    if (!isMessageFreshAndAuthentic(message)) {
                        printError("Ignoring invalid message...");
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
                    printError("A node did not respond within expected limits...");
                } else if (cause instanceof SignatureException) {
                    printError("Seller could not sign a message to be sent to at least one of the replicas...");
                }
            }
        }
        ONRRMajorityVoting.assertOperationSuccess(ackCount, "transferGood");
        print("Redirecting all messages to client...");
        return messagesList;
    }

    /***********************************************************
     * HELPERS
     ***********************************************************/

    private ResponseEntity<List<BasicMessage>> newSignedError(SaleRequestMessage requestMessage, String reason) {
        List<BasicMessage> responseEntityList = new ArrayList<>();
        try {
            responseEntityList.add(setMessageSignature(getMyPrivateKey(), newErrorResponse(requestMessage, reason)));
            return new ResponseEntity<>(responseEntityList, HttpStatus.MULTIPLE_CHOICES);
        } catch (SignatureException se) {
            return null;
        }
    }

    private BasicMessage newErrorResponse(BasicMessage receivedRequest, String reason) {
        return new ErrorResponse(
                generateTimestamp(),
                receivedRequest.getRequestID(), // callee requestId
                OPERATION,
                ClientProperties.getMyClientPort(), // from me
                receivedRequest.getFrom(),  // to callee
                "",
                "bad request.",
                reason
        );
    }
}
