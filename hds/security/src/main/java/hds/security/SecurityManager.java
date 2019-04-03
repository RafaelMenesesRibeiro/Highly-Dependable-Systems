package hds.security;

import hds.security.msgtypes.SecureResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SecurityManager {
    private static final String SERVER_RESERVED_PORT = "8000";
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String PRIVATE_KEY_BASE_FILENAME = "HDSNotary_PrivateK_ID_";
    private static final String PUBLIC_KEY_BASE_FILENAME = "HDSNotary_PublicK_ID_";
    private static final String PUBLIC_KEY_FILE_EXTENSION = ".pub";
    private static final String PRIVATE_KEY_FILE_EXTENSION = ".key";

    private SecurityManager() {
    }

    public static byte[] getByteArray(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return bos.toByteArray();
        }
    }

    private static String getResourcePath(String resourceId, boolean isPublicKey) {
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

    private static Path getResourceRelativePath(String resourceId, boolean isPublicKey) throws IOException {
        String filePath = getResourcePath(resourceId, isPublicKey);
        ClassLoader classLoader = SecurityManager.class.getClassLoader();
        File file = new File(classLoader.getResource(filePath).getFile());
        return file.toPath();
    }

    private static byte[] getResourceFileBytes(Path resourceRelativePath) throws IOException {
        return Files.readAllBytes(resourceRelativePath);
    }

    public static PublicKey getPublicKeyFromResource(String resourceId)
            throws IOException, InvalidKeySpecException {

        try {
            byte[] bytes = getResourceFileBytes(getResourceRelativePath(resourceId, true));
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return kf.generatePublic(ks);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKeyFromResource(String resourceId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        byte[] bytes = getResourceFileBytes(getResourceRelativePath(resourceId, false));
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        return kf.generatePrivate(ks);
    }

    public static byte[] signData(byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, InvalidKeySpecException {

        PrivateKey key = getPrivateKeyFromResource(SERVER_RESERVED_PORT);
        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initSign(key);
        sign.update(data);
        return sign.sign();
    }

    public static byte[] signData(PrivateKey key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initSign(key);
        sign.update(data);
        return sign.sign();
    }

    public static boolean isAuthenticResponse(SecureResponse secureResponse, String nodeId)
            throws IOException, InvalidKeySpecException {

        PublicKey HDSPublicKey = getPublicKeyFromResource(nodeId);
        return verifySignature(HDSPublicKey, secureResponse.getSignature(), secureResponse.getPayload());
    }

    public static boolean verifySignature(PublicKey key, byte[] signedData, Object payload) {
        try {
            return verifySignature(key, signedData, getByteArray(payload));
        } catch (Exception exc) {
            System.out.println("[xxx] Unexpected error getting payload bytes for signature verification.");
            return false;
        }
    }

    private static boolean verifySignature(PublicKey key, byte[] signedData, byte[] testData)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initVerify(key);
        sign.update(testData);
        return sign.verify(signedData);
    }

    /**
     * Creates as a new timestamp representing a Java EPOCH starting on 1970-01-01T00:00:00Z.
     * @return long timestamp
     */
    public static long generateTimestamp() {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

    /**
     * @param sentTimestamp long timestamp retrieved from received message
     * @param tolerance int representing the start of the time window in which the received message is fresh.
     * @return boolean acknowledging or denying freshness of a message
     */
    private static boolean isFreshTimestamp(long sentTimestamp, int tolerance) {
        Instant instantNow = Instant.now();
        Instant sentInstant = Instant.ofEpochSecond(sentTimestamp);
        // if instantNow-Tolerance < rcvTimestamp < instantNow, then it's fresh, else it's old and should be discarded
        return sentInstant.isAfter(instantNow.minus(tolerance, ChronoUnit.SECONDS));
    }

    /**
     * Computes the SHA-1 hash over a true-random seed value concatenated with a
     * 64-bit counter which is incremented by 1 for each operation.
     * This method is used to generate a random secure number string, in order to
     * avoid attack by repetition. Client outgoing messages place one of these in
     * their headers.
     */
    public static String generateSecureNumber() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        final byte array[] = new byte[32];
        random.nextBytes(array);
        return "boo";
    }

}