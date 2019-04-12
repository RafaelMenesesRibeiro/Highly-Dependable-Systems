package hds.security;

import hds.security.msgtypes.BasicMessage;
import sun.security.pkcs11.wrapper.PKCS11;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

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

    public static BasicMessage setMessageSignature(PrivateKey privateKey, BasicMessage message) throws SignatureException {
        message.setSignature(bytesToBase64String(signData(privateKey, message.toString().getBytes(Charset.forName("UTF-8")))));
        return message;
    }

    public static BasicMessage setMessageSignature(PKCS11 pkcs11, long ccSessionID, long ccSignatureKey, BasicMessage message) throws SignatureException {
        message.setSignature(bytesToBase64String(signData(pkcs11, ccSessionID, ccSignatureKey, message.toString().getBytes(Charset.forName("UTF-8")))));
        return message;
    }

    public static String isValidMessage(String selfId, BasicMessage message) {
        if (!selfId.equals(message.getTo()) &&
                !(message.getOperation().equals("getStateOfGood") && message.getTo().equals("unknown"))) {
            return "wrong host address";
        }

        if (!isFreshTimestamp(message.getTimestamp())) {
            return "message is more than five minutes old";
        }

        if (message.getFrom().equals("server")) {
            if (!isValidSignatureFromServer(message)) {
                return "invalid signature";
            }
        } else {
            if (!isValidSignatureFromNode(message)) {
                return "invalid signature";
            }
        }

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
}
