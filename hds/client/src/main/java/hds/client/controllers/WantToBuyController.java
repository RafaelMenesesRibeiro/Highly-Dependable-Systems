package hds.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.helpers.ClientProperties;
import hds.security.SecurityManager;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.domain.SignedTransactionData;
import hds.security.domain.TransactionData;
import hds.security.msgtypes.responses.ErrorResponse;
import hds.security.msgtypes.responses.SecureResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static hds.client.helpers.ConnectionManager.*;
import static hds.security.SecurityManager.*;

@RestController
public class WantToBuyController {

    private static final String OPERATION = "wantToBuy";

    @PostMapping(value = "/wantToBuy")
    public SecureResponse wantToBuy(@RequestBody SignedTransactionData signedTransactionData) {
        TransactionData transactionData = signedTransactionData.getPayload();
        String sellerID = transactionData.getSellerID();
        String buyerID = transactionData.getBuyerID();
        String goodID = transactionData.getGoodID();
        SecureResponse payload = null;

        try {
            payload = execute(signedTransactionData, sellerID, buyerID, goodID);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | IOException | InvalidKeySpecException | JSONException e) {
            payload = new SecureResponse(new ErrorResponse(ControllerErrorConsts.BAD_SELLER, OPERATION, "The seller node has thrown an exception"));
        }

        return payload;
    }

    private SecureResponse execute(SignedTransactionData signedTransactionData, String sellerID, String buyerID, String goodID)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, JSONException {
        PublicKey buyerPublicKey = getPublicKeyFromResource(buyerID);
        String buyerSignature = signedTransactionData.getBuyerSignature();

        // TODO Sign Transaction data and buyer signature
        PrivateKey sellerPrivateKey = getPrivateKeyFromResource(ClientProperties.getPort());

        if (!verifySignature(buyerPublicKey, buyerSignature, signedTransactionData.getPayload())) {
            signedTransactionData.getPayload().setBuyerID(SELLER_INCORRECT_BUYER_SIGNATURE);
        }

        byte[] sellerSignature = signData(sellerPrivateKey, getByteArray(signedTransactionData.getPayload()));
        signedTransactionData.setSellerSignature(bytesToBase64String(sellerSignature));

        String requestUrl = String.format("%s%s", ClientProperties.HDS_NOTARY_HOST, "transferGood");
        HttpURLConnection connection = initiatePOSTConnection(requestUrl);

        // TODO use buyGood methodology create an Object representing the payload, it must implement Serializable
        // TODO... then use ObjectMapper.writeAsStringValue or whatever the name of the method is.
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject requestData = new JSONObject(objectMapper.writeValueAsString(signedTransactionData));

        sendPostRequest(connection, requestData);
        hds.client.domain.SecureResponse domainSecureResponse = getSecureResponse(connection);
        return domainSecureResponse.translateSecureResponse();
    }

}
