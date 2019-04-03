package hds.client.controllers;

import hds.client.helpers.ClientProperties;
import hds.security.SecurityManager;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicResponse;
import hds.security.domain.SignedTransactionData;
import hds.security.domain.TransactionData;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SecureResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static hds.client.helpers.ConnectionManager.*;

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
        byte[] buyerSignature = signedTransactionData.getBuyerSignature();
        byte[] transactionDataBytes = SecurityManager.getByteArray(signedTransactionData.getPayload());
        PublicKey buyerPublicKey = SecurityManager.getPublicKeyFromResource(buyerID);
        PrivateKey sellerPrivateKey = SecurityManager.getPrivateKeyFromResource(ClientProperties.getPort());

        if (!SecurityManager.verifySignature(buyerPublicKey, buyerSignature, transactionDataBytes)) {
            signedTransactionData.getPayload().setBuyerID(SecurityManager.SELLER_INCORRECT_BUYER_SIGNATURE);
            transactionDataBytes = SecurityManager.getByteArray(signedTransactionData.getPayload());
        }

        byte[] sellerSignature = SecurityManager.signData(sellerPrivateKey, transactionDataBytes);
        signedTransactionData.setSellerSignature(sellerSignature);

        String requestUrl = String.format("%s%s", ClientProperties.HDS_NOTARY_HOST, "transferGood");
        HttpURLConnection connection = initiatePOSTConnection(requestUrl);

        JSONObject payload = new JSONObject();
        payload.put("sellerID", signedTransactionData.getPayload().getSellerID());
        payload.put("buyerID", signedTransactionData.getPayload().getBuyerID());
        payload.put("goodID", signedTransactionData.getPayload().getGoodID());

        JSONObject request = new JSONObject();
        request.put("buyerSignature", signedTransactionData.getBuyerSignature());
        request.put("sellerSignature", signedTransactionData.getSellerSignature());
        request.put("payload", payload);

        sendPostRequest(connection, request);
        return getSecureResponse(connection);
    }

}
