package hds.security;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
;import java.time.Instant;
import java.time.temporal.ChronoUnit;


public class SecurityManager {

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String PRIVATE_KEY_BASE_FILENAME = "HDSNotary_PrivateK_ID_";
    private static final String PUBLIC_KEY_BASE_FILENAME = "HDSNotary_PublicK_ID_";
    private static final String PUBLIC_KEY_FILE_EXTENSION = ".pub";
    private static final String PRIVATE_KEY_FILE_EXTENSION = ".key";

    private SecurityManager() {
    }

    /**
     * Makes constructor of this class not directly accessible
     */
    private static class SecurityManagerHolder {
        private static final SecurityManager INSTANCE = new SecurityManager();
    }

    private String getResourcePath(String resourceId, boolean isPublicKey) {
        StringBuilder filePath = new StringBuilder("keys/");
        if (isPublicKey) {
            filePath.append(PUBLIC_KEY_BASE_FILENAME);
            filePath.append(resourceId);
            filePath.append(PUBLIC_KEY_FILE_EXTENSION);
        }
        else {
            filePath.append(PRIVATE_KEY_BASE_FILENAME);
            filePath.append(resourceId);
            filePath.append(PRIVATE_KEY_FILE_EXTENSION);
        }
        return filePath.toString();
    }

    private File getResourceFile(String resourceId, boolean isPublicKey) throws IOException {
        String filePath = getResourcePath(resourceId, isPublicKey);
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(resourceId).getFile());
    }

    private static byte[] getResourceFileBytes(File resourceFile) throws IOException {
        return Files.readAllBytes(resourceFile.toPath());
    }

    public PublicKey getPublicKeyFromResource(String resourceId) throws IOException, InvalidKeySpecException {
        try {
            byte[] bytes = getResourceFileBytes(getResourceFile(resourceId, true));
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return kf.generatePublic(ks);
        } catch (NoSuchAlgorithmException nSAExc) {
            return null;
        }
    }

    public PrivateKey getPrivateKeyFromResource(String resourceId) throws IOException, InvalidKeySpecException {
        try {
            byte[] bytes = getResourceFileBytes(getResourceFile(resourceId,false));
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return kf.generatePrivate(ks);
        } catch (NoSuchAlgorithmException nSAExc) {
            return null;
        }
    }

    public byte[] signData(PrivateKey key, byte[] data) throws InvalidKeyException, SignatureException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initSign(key);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException nsaex) {
            return new byte[0];
        }
    }

    public boolean verifySignature(PublicKey key, byte[] signedData, byte[] testData)
            throws SignatureException, InvalidKeyException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initVerify(key);
            sign.update(testData);
            return sign.verify(signedData);
        } catch (NoSuchAlgorithmException nsaex) {
            return false;
        }
    }

    /**
     * Creates as a new timestamp representing a Java EPOCH starting on 1970-01-01T00:00:00Z.
     * @return long timestamp
     */
    public long generateTimestamp() {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

    /**
     * @param sentTimestamp long timestamp retrieved from received message
     * @param tolerance int representing the start of the time window in which the received message is fresh.
     * @return boolean acknowledging or denying freshness of a message
     */
    private boolean isFreshTimestamp(long sentTimestamp, int tolerance) {
        Instant instantNow = Instant.now();
        Instant sentInstant = Instant.ofEpochSecond(sentTimestamp);
        // if instantNow-Tolerance < rcvTimestamp < instantNow, then it's fresh, else it's old and should be discarded
        return sentInstant.isAfter(instantNow.minus(tolerance, ChronoUnit.SECONDS));
    }
}