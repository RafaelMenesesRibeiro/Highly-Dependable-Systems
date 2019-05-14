package hds.client.services;

import java.security.PrivateKey;
import java.security.PublicKey;

import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.ResourceManager.getPublicKeyFromResource;
import static org.junit.Assert.fail;

public abstract class BaseTests {

    public static final String C_1_PORT = "8001";
    public static final String C_2_PORT = "8002";
    public static final String C_3_PORT = "8003";
    public static final String S_1_PORT = "9000";
    public static final String S_2_PORT = "9001";
    public static final String S_3_PORT = "9002";
    public static final String S_4_PORT = "9003";

    public static final String GOOD_1 = "good1";
    public static final String GOOD_2 = "good2";
    public static final String GOOD_3 = "good3";
    public static final String GOOD_4 = "good4";

    public static final int RID_1 = 1;
    public static final int RID_2 = 2;
    public static final int RID_3 = 3;
    public static final int RID_4 = 4;

    public static final int WTS_1 = 1;
    public static final int WTS_2 = 2;
    public static final int WTS_3 = 3;
    public static final int WTS_4 = 4;

    public static PrivateKey c1PrivateKey;
    public static PublicKey c1PublicKey;
    public static PrivateKey c2PrivateKey;
    public static PublicKey c2PublicKey;
    public static PrivateKey c3PrivateKey;
    public static PublicKey c3PublicKey;
    public static PrivateKey s1PrivateKey;
    public static PublicKey s1PublicKey;
    public static PrivateKey s2PrivateKey;
    public static PublicKey s2PublicKey;
    public static PrivateKey s3PrivateKey;
    public static PublicKey s3PublicKey;
    public static PrivateKey s4PrivateKey;
    public static PublicKey s4PublicKey;

    static {
        try {
            c1PrivateKey = getPrivateKeyFromResource(C_1_PORT);
            c1PublicKey = getPublicKeyFromResource(C_1_PORT);
            c2PrivateKey = getPrivateKeyFromResource(C_2_PORT);
            c2PublicKey = getPublicKeyFromResource(C_2_PORT);
            c3PrivateKey = getPrivateKeyFromResource(C_3_PORT);
            c3PublicKey = getPublicKeyFromResource(C_3_PORT);
            s1PrivateKey = getPrivateKeyFromResource(S_1_PORT);
            s1PublicKey = getPublicKeyFromResource(S_1_PORT);
            s2PrivateKey = getPrivateKeyFromResource(S_2_PORT);
            s2PublicKey = getPublicKeyFromResource(S_2_PORT);
            s3PrivateKey = getPrivateKeyFromResource(S_3_PORT);
            s3PublicKey = getPublicKeyFromResource(S_3_PORT);
            s4PrivateKey = getPrivateKeyFromResource(S_4_PORT);
            s4PublicKey = getPublicKeyFromResource(S_4_PORT);
        } catch (Exception e) {
            fail("Could not load public/private key pairs from resource...");
        }
    }
}
