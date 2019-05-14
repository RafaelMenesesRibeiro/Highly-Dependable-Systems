package hds.security;

import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import org.json.JSONException;
import org.json.JSONObject;
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

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.*;
import static hds.security.DateUtils.isFreshTimestamp;
import static hds.security.ResourceManager.getCertificateFromResource;
import static hds.security.ResourceManager.getPublicKeyFromResource;

@SuppressWarnings("Duplicates")
public class SecurityManager {
    public static final String INITIAL_DATABASE_ENRTY_SIGNATURE = "initialSign";

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

    /** Given a basic message it verifies if it's fresh, if it's been seen before and if the signature is valid */
    public static String isValidMessage(BasicMessage message) {

        if (!isFreshTimestamp(message.getTimestamp())) {
            return "message is more than five minutes old";
        }

        // TODO - Add try here. If from cannot be parsed to int, this might crash the system. //
        int from = Integer.parseInt(message.getFrom());

        // Hammering for initial value signature validation
        if (message.getSignature().equals("initialSign")){
            return "";
        }

        if (from >= 10000) {
            // These servers use Citizen's Card for signing operations
            if (!isValidSignatureFromServer(message))
                return "invalid signature";
        }
        else if (from >= 9000 && from < 10000) {
            // These servers use regular public key and private keys for signing operations
            if (!isValidSignatureFromNode(message))
                return "invalid signature";
        }
        else if (from >= 8000 && from < 9000){
            // These behave like regular servers, we put them on a different conditional just for explicitly
            if (!isValidSignatureFromNode(message))
                return "invalid signature";
        }

        // Blank string means True, but we can't actually use a boolean.
        return "";
    }

    public static boolean isValidMessageFromServer(BasicMessage message) {
        try {
            int from = Integer.parseInt(message.getFrom());
            if (from >= 10000) {
                return isValidSignatureFromServer(message);
            }
            else if (from >= 9000) {
                return isValidSignatureFromNode(message);
            }
            else if (from >= 8000){
                return isValidSignatureFromNode(message);
            }
            return true;
        }
        catch (Exception ex) {
            return false;
        }
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

    public static boolean verifyWriteOnGoodsOperationSignature(final String goodId,
                                                               final Boolean value,
                                                               final String writerId,
                                                               final long wts,
                                                               final String signature) {

        try {
            JSONObject json = newWriteOnGoodsData(goodId, value, writerId, wts);
            PublicKey signersPublicKey = getPublicKeyFromResource(writerId);
            return authenticateSignatureWithPubKey(signersPublicKey, signature, json.toString());
        }
        catch (Exception exc) {
            return wts == 0 && signature.equals("initialSign");
        }
    }

    public static boolean verifyWriteOnGoodsDataResponseSignature(final String goodId,
                                                                  final Boolean value,
                                                                  final String writerId,
                                                                  final long wts,
                                                                  final String signature) {

        try {
            JSONObject json = newWriteOnGoodsDataResponse(goodId, value, writerId, wts);
            PublicKey signersPublicKey = getPublicKeyFromResource(writerId);
            return authenticateSignatureWithPubKey(signersPublicKey, signature, json.toString());
        }
        catch (Exception exc) {
            return wts == 0 && signature.equals("initialSign");
        }
    }

    public static boolean verifyWriteOnOwnershipSignature(final String goodID,
                                                          final String writerId,
                                                          final long wts,
                                                          final String signature) {

        try {
            JSONObject json = newWriteOnOwnershipData(goodID, writerId, wts);
            PublicKey signersPublicKey = getPublicKeyFromResource(writerId);
            return authenticateSignatureWithPubKey(signersPublicKey, signature, json.toString());
        }
        catch (Exception exc) {
            return wts == 0 && signature.equals("initialSign");
        }
    }

    public static JSONObject newWriteOnGoodsData(final String goodId,
                                                 final Boolean value,
                                                 final String writer,
                                                 final long wts) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodId", goodId);
        jsonObject.put("onSale", value);
        jsonObject.put("writer", writer);
        jsonObject.put("wts", wts);
        return  jsonObject;
    }

    public static JSONObject newWriteOnGoodsDataResponse(final String goodId,
                                                         final Boolean value,
                                                         final String writerId,
                                                         final long wts) throws JSONException {

        return newWriteOnGoodsData(goodId, value, writerId, wts);
    }

    public static JSONObject newWriteOnOwnershipData(final String goodId,
                                                     final String writerId,
                                                     final long wts) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodId", goodId);
        jsonObject.put("writerId", writerId);
        jsonObject.put("wts", wts);
        return  jsonObject;
    }
}
