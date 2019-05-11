package hds.client.controllers;

import hds.client.domain.TransferGoodCallable;
import hds.client.helpers.ClientProperties;
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
            futuresList = executorService.invokeAll(callableList);
        } catch (InterruptedException ie) {
            printError(ie.getMessage());
        }

        List<BasicMessage> basicMessageList = getBasicMessagesFromFutures(futuresList);
        ResponseEntity<List<ResponseEntity<BasicMessage>>> httpResponse = processNotaryResponses(basicMessageList);

        executorService.shutdown();

        return httpResponse;
    }

    private List<BasicMessage> getBasicMessagesFromFutures(List<Future<BasicMessage>> futuresList) {
        List<BasicMessage> basicMessageList = new ArrayList<>();
        for (Future<BasicMessage> future : futuresList) {
            try {
                BasicMessage resultContent = future.get();
                basicMessageList.add(resultContent);
            } catch (InterruptedException ie) {
                printError(ie.getMessage());
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof SocketTimeoutException) {
                    printError("A node did not respond within expected limits...");
                } else if (cause instanceof SignatureException) {
                    printError("Seller could not sign a message to be sent to at least one of the replicas...");
                } else {
                    printError(cause.getMessage());
                }
            }
        }
        return  basicMessageList;
    }

    private ResponseEntity<List<ResponseEntity<BasicMessage>>> processNotaryResponses(List<BasicMessage> basicMessageList) {

        List<ResponseEntity<BasicMessage>> responseEntityList = new ArrayList<>();
        for (BasicMessage message : basicMessageList) {
            String validationResult = isValidMessage(message);
            if (!validationResult.equals("")) {
                String reason = "Seller encountered error validating response from notary: " + validationResult;
                responseEntityList.add(
                        new ResponseEntity<>(newErrorResponse(message, reason), HttpStatus.UNAUTHORIZED)
                );
            } else {
                responseEntityList.add(new ResponseEntity<>(message, HttpStatus.OK));
                System.out.println("[o] " + message.toString());
            }
        }
        return new ResponseEntity<>(responseEntityList, HttpStatus.MULTIPLE_CHOICES);
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
