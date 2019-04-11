package hds.security;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class CryptoUtils {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /***********************************************************
     *
     * PUBLIC METHODS RELATED WITH SIGNATURE CREATION
     *
     ***********************************************************/

    public static byte[] signData(PrivateKey key, byte[] data) throws SignatureException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initSign(key);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException exc) {
            throw new SignatureException(exc.getMessage());
        }
    }

    /***********************************************************
     *
     * PUBLIC METHODS RELATED WITH SIGNATURE VERIFICATION
     *
     ***********************************************************/

    public static boolean authenticateSignature(PublicKey key, String signedData, Object payload) throws IOException, SignatureException {
        return authenticateSignature(key, ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
    }

    public static boolean authenticateSignatureWithCert(X509Certificate certificate, String signedData, Object payload)
            throws IOException, CertificateException, SignatureException {
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException exc) {
            throw new CertificateException(exc.getMessage());
        }
        return authenticateSignature(certificate.getPublicKey(), ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
    }

    /***********************************************************
     *
     * PRIVATE METHODS RELATED WITH SIGNATURE VERIFICATION
     *
     ***********************************************************/

    private static boolean authenticateSignature(PublicKey key, byte[] signedData, byte[] testData) throws SignatureException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initVerify(key);
            sign.update(testData);
            return sign.verify(signedData);
        } catch (NoSuchAlgorithmException | InvalidKeyException exc) {
            throw new SignatureException(exc.getMessage());
        }
    }

    /***********************************************************
     *
     * HELPER METHODS NOT EXPLICITLY RELATED WITH SECURITY
     *
     ***********************************************************/

    public static String newUUIDString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
