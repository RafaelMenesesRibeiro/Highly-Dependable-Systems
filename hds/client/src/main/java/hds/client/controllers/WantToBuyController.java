package hds.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import hds.client.exceptions.ResponseMessageException;
import hds.client.helpers.ClientProperties;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SaleRequestMessage;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SignatureException;

import static hds.client.helpers.ClientProperties.HDS_NOTARY_HOST;
import static hds.client.helpers.ClientProperties.getPrivateKey;
import static hds.client.helpers.ConnectionManager.*;
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.isValidMessage;
import static hds.security.SecurityManager.setMessageWrappingSignature;

@RestController
public class WantToBuyController {

    private static final String OPERATION = "wantToBuy";

    @PostMapping(value = "/wantToBuy")
    public ResponseEntity<BasicMessage> wantToBuy(@RequestBody SaleRequestMessage requestMessage) {
        String validationResult = isValidMessage(ClientProperties.getPort(), requestMessage);
        if (!"".equals(validationResult)) {
            return new ResponseEntity<>(newErrorResponse(requestMessage, "The seller has encountered the following error validating request from node :" + validationResult + "."), HttpStatus.valueOf(401));
        }
        return execute(requestMessage);
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

    private ResponseEntity<BasicMessage> execute(SaleRequestMessage requestMessage) {
        ApproveSaleRequestMessage message = new ApproveSaleRequestMessage(
                requestMessage.getTimestamp(),
                requestMessage.getRequestID(),
                requestMessage.getOperation(),
                requestMessage.getFrom(),
                requestMessage.getTo(),
                requestMessage.getSignature(),
                requestMessage.getGoodID(),
                requestMessage.getBuyerID(),
                requestMessage.getSellerID(),
                generateTimestamp(),
                "transferGood",
                ClientProperties.getPort(),
                ClientProperties.HDS_NOTARY_PORT,
                ""
        );

        try {
            setMessageWrappingSignature(getPrivateKey(), message);
        } catch (SignatureException e) {
            return new ResponseEntity<>(newErrorResponse(requestMessage, "The seller has thrown an exception while signing the message."), HttpStatus.valueOf(500));
        }

        try {
            HttpURLConnection connection = initiatePOSTConnection(HDS_NOTARY_HOST + "transferGood");
            sendPostRequest(connection, newJSONObject(message));
            BasicMessage responseMessage = getResponseMessage(connection, Expect.SALE_CERT_RESPONSE);
            String validationResult = isValidMessage(requestMessage.getFrom(), responseMessage);
            if (!validationResult.equals("")) {
                return new ResponseEntity<>(newErrorResponse(requestMessage, "The seller has encountered the following error validating response from server :" + validationResult + "."), HttpStatus.valueOf(401));
            }
            System.out.println("[o] " + responseMessage.toString());
            return new ResponseEntity<>(responseMessage, HttpStatus.valueOf(connection.getResponseCode()));
        } catch (JsonProcessingException | JSONException e) {
            return new ResponseEntity<>(newErrorResponse(requestMessage, "The seller has thrown an exception while creating the json to send to the notary."), HttpStatus.valueOf(500));
        } catch (IOException e) {
            return new ResponseEntity<>(newErrorResponse(requestMessage, "The seller has thrown an exception while reading/writing message from/to notary."), HttpStatus.valueOf(500));
        }
    }
}
