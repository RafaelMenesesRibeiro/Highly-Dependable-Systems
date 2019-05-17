package hds.security.ut;

import java.security.PrivateKey;
import java.security.PublicKey;

import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.ResourceManager.getPublicKeyFromResource;
import static org.junit.Assert.fail;

abstract class BaseUT {

    private static final String C_1_PORT = "8001";
    private static final String C_2_PORT = "8002";

    static PrivateKey c1PrivateKey;
    static PublicKey c1PublicKey;
    static PrivateKey c2PrivateKey;
    static PublicKey c2PublicKey;

    static String testData = "TEST";

    static String wrongData = "WRONG_TEST";
    static String malformedSignature = "MalformedSignature";

    static {
        try {
            c1PrivateKey = getPrivateKeyFromResource(C_1_PORT);
            c1PublicKey = getPublicKeyFromResource(C_1_PORT);
            c2PrivateKey = getPrivateKeyFromResource(C_2_PORT);
            c2PublicKey = getPublicKeyFromResource(C_2_PORT);
        } catch (Exception e) {
            fail("Could not load public/private key pairs from resource...");
        }
    }
}
