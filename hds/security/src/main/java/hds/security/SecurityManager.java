package hds.security;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public class SecurityManager {
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String PRIVATE_KEY_BASE_FILENAME = "HDSNotary_PrivateK_ID_";
    private static final String PUBLIC_KEY_BASE_FILENAME = "HDSNotary_PublicK_ID_";
    private static final String PUBLIC_KEY_FILE_EXTENSION = ".pub";
    private static final String PRIVATE_KEY_FILE_EXTENSION = ".key";

    @Deprecated
    public static final String SELLER_INCORRECT_BUYER_SIGNATURE = "-2";

    private SecurityManager() {}

    public static PublicKey getPublicKeyFromResource(String resourceId) throws IOException, InvalidKeySpecException {
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

    private static String buildResourcePath(String resourceId, boolean isPublicKey) {
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

    private static Path getResourceRelativePath(String resourceId, boolean isPublicKey) {
        String filePath = buildResourcePath(resourceId, isPublicKey);
        ClassLoader classLoader = SecurityManager.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(filePath)).getFile());
        return file.toPath();
    }

    private static byte[] getResourceFileBytes(Path resourceRelativePath) throws IOException {
        return Files.readAllBytes(resourceRelativePath);
    }

}