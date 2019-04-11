package hds.security;

import hds.security.msgtypes.BasicMessage;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    public static boolean isAuthenticResponseFromClient(BasicMessage secureResponse, String nodeId)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        PublicKey HDSPublicKey = ResourceManager.getPublicKeyFromResource(nodeId);
        return authenticateSignature(HDSPublicKey, secureResponse.getSignature(), secureResponse.getPayload());
    }

    public static boolean isAuthenticResponseFromServer(BasicMessage secureResponse) throws CertificateException, IOException {
        X509Certificate serverCert = ResourceManager.getCertificateFromResource();
        return authenticateSignature(serverCert, secureResponse.getSignature(), secureResponse.getPayload());
    }

    public static byte[] signData(PrivateKey key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initSign(key);
        sign.update(data);
        return sign.sign();
    }

    private static boolean authenticateSignature(PublicKey key, byte[] signedData, byte[] testData)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initVerify(key);
        sign.update(testData);
        return sign.verify(signedData);
    }

    public static boolean authenticateSignature(PublicKey key, String signedData, Object payload) {
        try {
            return authenticateSignature(key, ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
        } catch (Exception exc) {
            System.out.println("[xxx] Unexpected error getting payload bytes for signature verification.");
            return false;
        }
    }

    public static boolean authenticateSignature(X509Certificate certificate, String signedData, Object payload) {
        try {
            certificate.checkValidity();
            return authenticateSignature(certificate.getPublicKey(), ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
        } catch (Exception exc) {
            System.out.println("[xxx] Unexpected error getting payload bytes for signature verification.");
            return false;
        }
    }
}
