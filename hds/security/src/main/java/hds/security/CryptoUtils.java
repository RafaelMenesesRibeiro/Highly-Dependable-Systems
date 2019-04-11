package hds.security;

import hds.security.msgtypes.BasicMessage;

import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.UUID;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    public static String newUUIDString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static boolean isAuthenticResponseFromClient(BasicMessage secureResponse, String nodeId) throws Exception {
        PublicKey HDSPublicKey = ResourceManager.getPublicKeyFromResource(nodeId);
        /*
        return authenticateSignature(HDSPublicKey, secureResponse.getSignature(), secureResponse.getPayload());*/
        return true;
    }

    public static boolean isAuthenticResponseFromServer(BasicMessage secureResponse) throws Exception {
        X509Certificate serverCert = ResourceManager.getCertificateFromResource();/*
        return authenticateSignature(serverCert, secureResponse.getSignature(), secureResponse.getPayload());*/
        return true;
    }

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

    public static boolean authenticateSignature(PublicKey key, String signedData, Object payload) throws IOException, SignatureException {
        return authenticateSignature(key, ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
    }

    public static boolean authenticateSignature(X509Certificate certificate, String signedData, Object payload)
            throws IOException, CertificateException, SignatureException {
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException exc) {
            throw new CertificateException(exc.getMessage());
        }
        return authenticateSignature(certificate.getPublicKey(), ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
    }
}
