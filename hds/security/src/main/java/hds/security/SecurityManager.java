package hds.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class SecurityManager {

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String PRIVATE_KEY_BASE_FILENAME = "HDSNotary_PrivateK_ID_";
    private static final String PUBLIC_KEY_BASE_FILENAME = "HDSNotary_PublicK_ID_";
    private static final String PUBLIC_KEY_FILE_EXTENSION = ".pub";
    private static final String PRIVATE_KEY_FILE_EXTENSION = ".key";

    private SecurityManager() {
        // This is here so the class can't be instantiated. //
    }

    // -------------------------------------------------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------------------------- //
    // PUBLIC / PRIVATE KEYS LOADING																				  //
    // -------------------------------------------------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------------------------- //

    private static byte[] getBytesFromFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    public static PublicKey loadPublicKeyFromID(String id)
            throws IOException, InvalidKeySpecException {
        return loadPublicKeyFromFile(PUBLIC_KEY_BASE_FILENAME + id + PUBLIC_KEY_FILE_EXTENSION);
    }

    public static PublicKey loadPublicKeyFromFile(String filename)
            throws IOException, InvalidKeySpecException {
        try {
            byte[] bytes = getBytesFromFile(filename);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return kf.generatePublic(ks);
        } catch (NoSuchAlgorithmException nsaex) {
            // Should never be here. The algorithm is a constant and known to work. //
            return null;
        }
    }

    public static PrivateKey loadPrivateKeyFromID(String id)
            throws IOException, InvalidKeySpecException {
        return loadPrivateKeyFromFile(PRIVATE_KEY_BASE_FILENAME + id + PRIVATE_KEY_FILE_EXTENSION);
    }

    public static PrivateKey loadPrivateKeyFromFile(String filename)
            throws IOException, InvalidKeySpecException {
        try {
            byte[] bytes = getBytesFromFile(filename);
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return kf.generatePrivate(ks);
        } catch (NoSuchAlgorithmException nsaex) {
            // Should never be here. The algorithm is a constant and known to work. //
            return null;
        }
    }


    // -------------------------------------------------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------------------------- //
    // PRIVATE KEY SIGNING																							  //
    // -------------------------------------------------------------------------------------------------------------- //
    // -------------------------------------------------------------------------------------------------------------- //
    public static byte[] privateKeySigning(PrivateKey pk, byte[] data)
            throws InvalidKeyException, SignatureException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initSign(pk);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException nsaex) {
            // Should never be here. The algorithm is a constant and known to work. //
            return new byte[0];
        }
    }

    public static boolean publicKeySignatureVerify(PublicKey pk, byte[] signedData, byte[] testData)
            throws SignatureException, InvalidKeyException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initVerify(pk);
            sign.update(testData);
            return sign.verify(signedData);
        } catch (NoSuchAlgorithmException nsaex) {
            // Should never be here. The algorithm is a constant and known to work. //
            return false;
        }
    }

}