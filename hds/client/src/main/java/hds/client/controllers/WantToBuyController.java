package hds.client.controllers;

import hds.client.domain.TransferGoodCallable;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ClientSecurityManager;
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
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.isValidMessage;

@RestController
public class WantToBuyController {

    private static final String OPERATION = "wantToBuy";

    @PostMapping(value = "/wantToBuy")
    public ResponseEntity<List<ResponseEntity<BasicMessage>>>wantToBuy(@RequestBody SaleRequestMessage requestMessage) {
        String validationResult = isValidMessage(requestMessage);
        if (!"".equals(validationResult)) {
            List<ResponseEntity<BasicMessage>> responseEntityList = new ArrayList<>();
            responseEntityList.add(
                    new ResponseEntity<>(
                            newErrorResponse(requestMessage, "Seller found error in incoming request:" + validationResult),
                            HttpStatus.UNAUTHORIZED
                    )
            );
            return new ResponseEntity<>(responseEntityList, HttpStatus.MULTIPLE_CHOICES);
        }
        return tryDoTransfer(requestMessage);
    }

    private ResponseEntity<List<ResponseEntity<BasicMessage>>> tryDoTransfer(SaleRequestMessage requestMessage) {

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

        List<BasicMessage> basicMessageList = processTransferGoodResponses(requestMessage.getWts(), futuresList);
        ResponseEntity<List<ResponseEntity<BasicMessage>>> httpResponse = processNotaryResponses(basicMessageList);

        executorService.shutdown();

        return httpResponse;
    }

    private List<BasicMessage> processTransferGoodResponses(long wts, List<Future<BasicMessage>> futuresList) {
        List<BasicMessage> messagesList = new ArrayList<>();
        int ackCount = 0;
        for (Future<BasicMessage> future : futuresList) {
            if (!future.isCancelled()) {
                try {
                    BasicMessage message = future.get();
                    messagesList.add(message);
                    if (!ClientSecurityManager.isMessageFreshAndAuthentic(message)) {
                        printError("Ignoring invalid message...");
                        continue;
                    }
                    ackCount += ONRRMajorityVoting.isWriteResponseAcknowledge(wts, message);
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
