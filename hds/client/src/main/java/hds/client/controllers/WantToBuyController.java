package hds.client.controllers;

import hds.client.domain.TransferGoodCallable;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ONRRMajorityVoting;
import hds.security.msgtypes.BasicMessage;
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
import java.util.List;
import java.util.concurrent.*;

import static hds.client.helpers.ClientProperties.printError;
import static hds.client.helpers.ClientProperties.print;
import static hds.client.helpers.ClientSecurityManager.isMessageFreshAndAuthentic;
import static hds.security.DateUtils.generateTimestamp;

@RestController
public class WantToBuyController {

    private static final String OPERATION = "wantToBuy";

    @PostMapping(value = "/wantToBuy")
    public ResponseEntity<List<BasicMessage>>wantToBuy(@RequestBody SaleRequestMessage requestMessage) {
        if (isMessageFreshAndAuthentic(requestMessage)) {
            return tryDoTransfer(requestMessage);
        }
        List<BasicMessage> responseEntityList = new ArrayList<>();
        responseEntityList.add(newErrorResponse(requestMessage, "Seller rejects message, it's either not fresh or not properly signed"));
        return new ResponseEntity<>(responseEntityList, HttpStatus.MULTIPLE_CHOICES);
    }

    private ResponseEntity<List<BasicMessage>> tryDoTransfer(SaleRequestMessage requestMessage) {

        final List<String> replicasList = ClientProperties.getNotaryReplicas();
        final List<Callable<BasicMessage>> callableList = new ArrayList<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(replicasList.size());
        final long timestamp = generateTimestamp();

        for (String replicaId : replicasList) {
            callableList.add(new TransferGoodCallable(timestamp, replicaId, requestMessage));
        }

        List<Future<BasicMessage>> futuresList = new ArrayList<>();

        try {
            futuresList = executorService.invokeAll(callableList, 20, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            printError(ie.getMessage());
        }

        List<BasicMessage> transferGoodResponses = processTransferGoodResponses(requestMessage.getWts(), futuresList);

        executorService.shutdown();

        return new ResponseEntity<>(transferGoodResponses, HttpStatus.MULTIPLE_CHOICES);
    }

    private List<BasicMessage> processTransferGoodResponses(long wts, List<Future<BasicMessage>> futuresList) {
        List<BasicMessage> messagesList = new ArrayList<>();
        int ackCount = 0;
        for (Future<BasicMessage> future : futuresList) {
            if (!future.isCancelled()) {
                try {
                    BasicMessage message = future.get();
                    messagesList.add(message);
                    if (!isMessageFreshAndAuthentic(message)) {
                        printError("Ignoring invalid message...");
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.iwWriteAcknowledge(wts, message);
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
        }
        ONRRMajorityVoting.assertOperationSuccess(ackCount, "transferGood");
        print("Redirecting all messages to client...");
        return messagesList;
    }

    private BasicMessage newErrorResponse(BasicMessage receivedRequest, String reason) {
        return new ErrorResponse(
                generateTimestamp(),
                receivedRequest.getRequestID(), // callee requestId
                OPERATION,
                ClientProperties.getPort(), // from me
                receivedRequest.getFrom(),  // to callee
                "",
                "bad request.",
                reason
        );
    }
}
