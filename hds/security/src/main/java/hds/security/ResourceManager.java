package hds.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Objects;

public class ResourceManager {
    private static int SERVER_PORT;
    private static int MAX_CLIENT_ID;
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String KEYS_FOLDER_LOCATION = "keys/";
    private static final String PRIVATE_KEY_BASE_FILENAME = "HDSNotary_PrivateK_ID_";
    private static final String PUBLIC_KEY_BASE_FILENAME = "HDSNotary_PublicK_ID_";
    private static final String PUBLIC_KEY_FILE_EXTENSION = ".pub";
    private static final String PRIVATE_KEY_FILE_EXTENSION = ".key";
    private static final String SERVER_CERTIFICATE_LOCATION = "certs/server.pem";

    private ResourceManager() {}

    public static int getServerPort() {
        return SERVER_PORT;
    }

    public static void setServerPort(int serverPort) {
        SERVER_PORT = serverPort;
    }

    public static int getMaxClientId() {
        return MAX_CLIENT_ID;
    }

    public static void setMaxClientId(int maxClientId) {
        MAX_CLIENT_ID = maxClientId;
    }

    public static PublicKey getPublicKeyFromResource(String resourceId) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] bytes = getResourceFileBytes(getResourceRelativePath(resourceId, true));
        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        return kf.generatePublic(ks);
    }

    static X509Certificate getCertificateFromResource() throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<String> certEncoded = Files.readAllLines(getResourceRelativePath(SERVER_CERTIFICATE_LOCATION));
        byte[] certBytes = ConvertUtils.base64StringToBytes(certEncoded.get(1));
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    public static PrivateKey getPrivateKeyFromResource(String resourceId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        byte[] bytes = getResourceFileBytes(getResourceRelativePath(resourceId, false));
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        return kf.generatePrivate(ks);
    }

    private static Path getResourceRelativePath(String resourceId, boolean isPublicKey) {
        String filePath = buildResourcePath(resourceId, isPublicKey);
        ClassLoader classLoader = ResourceManager.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(filePath)).getFile());
        return file.toPath();
    }

    private static Path getResourceRelativePath(String resourcePath) {
        ClassLoader classLoader = ResourceManager.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(resourcePath)).getFile());
        return file.toPath();
    }

    private static byte[] getResourceFileBytes(Path resourceRelativePath) throws IOException {
        return Files.readAllBytes(resourceRelativePath);
    }

    private static String buildResourcePath(String resourceId, boolean isPublicKey) {
        StringBuilder filePath = new StringBuilder(KEYS_FOLDER_LOCATION);
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
}