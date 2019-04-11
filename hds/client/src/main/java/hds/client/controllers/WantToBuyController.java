package hds.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hds.client.helpers.ClientProperties;
import hds.security.ConvertUtils;
import hds.security.CryptoUtils;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.SaleCertificateResponse;
import hds.security.msgtypes.SaleRequestMessage;
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
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.ResourceManager.*;
import static hds.security.SecurityManager.isValidMessage;

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
        // TODO See your old execute method
        return new SaleCertificateResponse(); // Whatever is returned by the server may even be an ErrorResponse
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
