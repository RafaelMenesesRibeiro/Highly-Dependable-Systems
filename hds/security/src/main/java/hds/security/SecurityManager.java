package hds.security;

import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;

import sun.security.pkcs11.wrapper.PKCS11;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import org.json.JSONException;
import org.json.JSONObject;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.*;
import static hds.security.DateUtils.isFreshTimestamp;
import static hds.security.ResourceManager.getCertificateFromResource;
import static hds.security.ResourceManager.getPublicKeyFromResource;

public class SecurityManager {

    /***********************************************************
     *
     * PUBLIC METHODS
     *
     ***********************************************************/

    public static BasicMessage setMessageWrappingSignature(PrivateKey privateKey, ApproveSaleRequestMessage message) throws SignatureException {
        message.setWrappingSignature(bytesToBase64String(signData(privateKey, message.toString().getBytes(Charset.forName("UTF-8")))));
        return message;
    }


    public static BasicMessage setMessageSignature(PrivateKey privateKey, BasicMessage message) throws SignatureException {
        message.setSignature(bytesToBase64String(signData(privateKey, message.toString().getBytes(Charset.forName("UTF-8")))));
        return message;
    }

    public static BasicMessage setMessageSignature(PKCS11 pkcs11, long ccSessionID, long ccSignatureKey, BasicMessage message) throws SignatureException {
        message.setSignature(bytesToBase64String(signData(pkcs11, ccSessionID, ccSignatureKey, message.toString().getBytes(Charset.forName("UTF-8")))));
        return message;
    }

    public static String isValidMessage(String selfId, BasicMessage message) {

        // TODO - THIS IS SHIT. THIS ONLY WORKS FOR 30% OF OPERATIONS. DON'T TRY TO SAVE LINES. //
        /*
        if (!selfId.equals(message.getTo()) &&
                !(message.getOperation().equals("getStateOfGood") && message.getTo().equals("unknown"))) {
            return "wrong host address";
        }
        */

        if (message.getOperation().equals("getStateOfGood") && !message.getTo().equals("unknown")) {
            return "wrong host address";
        }
        else if (message.getOperation().equals("markForSale") && !message.getTo().equals(selfId)) {
            return "wrong host address";
        }
        else if (message.getOperation().equals("transferGood")) {
            // TODO //
        }
        else if (message.getOperation().equals("buyGood")) {
            // TODO //
        }


        if (!isFreshTimestamp(message.getTimestamp())) {
            return "message is more than five minutes old";
        }

        int from = Integer.parseInt(message.getFrom());

        if (from >= 10000) {
            if (!isValidSignatureFromServer(message))
                return "invalid signature";
        }
        else if (from >= 9000 && from < 10000) {
            if (!isValidSignatureFromNode(message))
                return "invalid signature";
        }
        else if (from >= 8000 && from < 9000){
            if (!isValidSignatureFromNode(message))
                return "invalid signature";
        }

        // Blank string means True, but we can't actually use a boolean.
        return "";
    }

    /***********************************************************
     *
     * PRIVATE METHODS
     *
     ***********************************************************/

    private static boolean isValidSignatureFromNode(BasicMessage message) {
        try {
            PublicKey signersPublicKey = getPublicKeyFromResource(message.getFrom());
            String signature = message.getSignature();
            message.setSignature("");
            boolean result = authenticateSignatureWithPubKey(signersPublicKey, signature, message.toString());
            message.setSignature(signature);
            return result;
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | SignatureException exc) {
            return false;
        }
    }

    private static boolean isValidSignatureFromServer(BasicMessage message) {
        try {
            X509Certificate serverCertificate = getCertificateFromResource();
            String signature = message.getSignature();
            message.setSignature("");
            boolean result = authenticateSignatureWithCert(serverCertificate, signature, message.toString());
            message.setSignature(signature);
            return result;
        } catch (IOException | SignatureException | CertificateException exc) {
            return false;
        }
    }

    /***********************************************************
     *
     * HELPER METHODS
     *
     ***********************************************************/

    public static JSONObject newWriteOnGoodsData(final String goodId,
                                                 final Boolean value,
                                                 final String writer,
                                                 final int logicalTimestamp) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodId", goodId);
        jsonObject.put("onSale", value);
        jsonObject.put("writer", writer);
        jsonObject.put("ts", logicalTimestamp);
        return  jsonObject;
    }

    public static JSONObject newWriteOnOwnershipsData(final String goodId,
                                                      final String writer,
                                                      final int logicalTimestamp) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodId", goodId);
        jsonObject.put("writer", writer);
        jsonObject.put("ts", logicalTimestamp);
        return  jsonObject;
    }
}
