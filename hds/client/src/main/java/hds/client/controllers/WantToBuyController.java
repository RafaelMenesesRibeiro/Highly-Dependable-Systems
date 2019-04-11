package hds.client.controllers;

import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import hds.client.exceptions.ResponseMessageException;
import hds.client.helpers.ClientProperties;
import hds.security.msgtypes.*;
import org.json.JSONException;
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
import static hds.security.SecurityManager.setMessageSignature;

@RestController
public class WantToBuyController {

    private static final String OPERATION = "wantToBuy";

    @PostMapping(value = "/wantToBuy")
    public BasicMessage wantToBuy(@RequestBody SaleRequestMessage requestMessage) {
        String validationResult = isValidMessage(ClientProperties.getPort(), requestMessage);
        if (!"".equals(validationResult)) {
            return newErrorResponse(requestMessage, validationResult);
        }
        return trySell(requestMessage);
    }

    private BasicMessage trySell(SaleRequestMessage requestMessage) {
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
                "bad request",
                reason
        );
    }

    private BasicMessage execute(SaleRequestMessage requestMessage) {
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
            setMessageSignature(getPrivateKey(), message);
        } catch (IOException | SignatureException e) {
            return newErrorResponse(requestMessage, "The seller has thrown an exception while signing the message");
        }

        try {
            HttpURLConnection connection = initiatePOSTConnection(HDS_NOTARY_HOST + "transferGood");
            sendPostRequest(connection, newJSONObject(message));
            BasicMessage responseMessage = getResponseMessage(connection, Expect.SALE_CERT_RESPONSE);
            String validationResult = isValidMessage(requestMessage.getFrom(), responseMessage);
            if (!validationResult.equals("")) {
                return newErrorResponse(requestMessage, "The seller has encountered the following error validating response from server :" + validationResult);
            }

            System.out.println("[o] " + responseMessage.toString());
            return responseMessage;
        } catch (JsonProcessingException | JSONException e) {
            return newErrorResponse(requestMessage, "The seller has thrown an exception while creating the json to send to the notary");
        } catch (IOException e) {
            return newErrorResponse(requestMessage, "The seller has thrown an exception while reading/writing message from/to notary");
        } catch (ResponseMessageException e) {
            return newErrorResponse(requestMessage, "The seller has thrown an exception while receiving the response from notary");
        }
    }

    /*
    private BasicMessage execute( SaleRequestMessage buyerRequestMessage)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, JSONException {
        PublicKey buyerPublicKey = getPublicKeyFromResource(buyerID);
        String buyerSignature = signedTransactionData.getBuyerSignature();
        PrivateKey sellerPrivateKey = getPrivateKeyFromResource(ClientProperties.getPort());
        if (!CryptoUtils.authenticateSignature(buyerPublicKey, buyerSignature, signedTransactionData.getPayload())) {
            signedTransactionData.getPayload().setBuyerID(SELLER_INCORRECT_BUYER_SIGNATURE);
        }
        byte[] sellerSignature = CryptoUtils.signData(sellerPrivateKey, ConvertUtils.objectToByteArray(signedTransactionData.getPayload()));
        signedTransactionData.setSellerSignature(ConvertUtils.bytesToBase64String(sellerSignature));
        String requestUrl = String.format("%s%s", ClientProperties.HDS_NOTARY_HOST, "transferGood");
        HttpURLConnection connection = initiatePOSTConnection(requestUrl);
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject requestData = new JSONObject(objectMapper.writeValueAsString(signedTransactionData));
        sendPostRequest(connection, requestData);
        hds.client.domain.SecureResponse domainSecureResponse = getSecureResponse(connection);
        return domainSecureResponse.translateSecureResponse();
   }
   */
}
