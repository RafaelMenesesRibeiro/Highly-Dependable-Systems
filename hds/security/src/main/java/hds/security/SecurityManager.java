package hds.security;

import hds.security.msgtypes.BasicMessage;
import sun.security.pkcs11.wrapper.PKCS11;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.ConvertUtils.objectToByteArray;
import static hds.security.CryptoUtils.authenticateSignature;
import static hds.security.CryptoUtils.signData;
import static hds.security.DateUtils.isFreshTimestamp;
import static hds.security.ResourceManager.getPublicKeyFromResource;

public class SecurityManager {

    /***********************************************************
     *
     * PUBLIC METHODS
     *
     ***********************************************************/

    public static BasicMessage setMessageSignature(PrivateKey privateKey, BasicMessage message)
            throws IOException, SignatureException {
        message.setSignature(bytesToBase64String(signData(privateKey, objectToByteArray(message))));
        return message;
    }

    public static BasicMessage setMessageSignature(PKCS11 pkcs11, long ccSessionID, long ccSignatureKey, BasicMessage message) throws IOException, SignatureException {
        message.setSignature(bytesToBase64String(signData(pkcs11, ccSessionID, ccSignatureKey, objectToByteArray(message))));
        return message;
    }

    public static String isValidMessage(String selfId, BasicMessage message) {
        if (!selfId.equals(message.getTo())) {
            return "wrong host address";
        }

        if (!isFreshTimestamp(message.getTimestamp())) {
            return "message is more than five minutes old";
        }

        if (!isValidSignature(message)) {
            return "invalid signature";
        }

        return "";
    }

    /***********************************************************
     *
     * PRIVATE METHODS
     *
     ***********************************************************/

    private static boolean isValidSignature(BasicMessage message) {
        try {
            PublicKey signersPublicKey = getPublicKeyFromResource(message.getFrom());
            String signature = message.getSignature();
            message.setSignature("");
            boolean result = authenticateSignature(signersPublicKey, signature, message);
            message.setSignature(signature);
            return result;
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | SignatureException exc) {
            return false;
        }
    }
}
