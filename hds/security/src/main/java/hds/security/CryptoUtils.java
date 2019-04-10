package hds.security;

import java.security.*;

public class CryptoUtils {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    static boolean authenticateSignature(PublicKey key, byte[] signedData, byte[] testData)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initVerify(key);
        sign.update(testData);
        return sign.verify(signedData);
    }

    public static byte[] signData(PrivateKey key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initSign(key);
        sign.update(data);
        return sign.sign();
    }

    public static boolean authenticateSignature(PublicKey key, String signedData, Object payload) {
        try {
            return authenticateSignature(key, ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
        } catch (Exception exc) {
            System.out.println("[xxx] Unexpected error getting payload bytes for signature verification.");
            return false;
        }
    }
}
