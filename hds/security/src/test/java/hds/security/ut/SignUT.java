package hds.security.ut;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.security.SignatureException;

import static hds.security.CryptoUtils.signData;

public class SignUT extends BaseUT {

    private static byte[] testDataBytes = testData.getBytes(Charset.forName("UTF-8"));

    @Test
    public void success() {
        try {
            byte[] signature = signData(c1PrivateKey, testDataBytes);
            Assert.assertNotNull(signature);
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test(expected = SignatureException.class)
    public void nullKey() throws SignatureException {
        signData(null, testDataBytes);
    }

    @Test(expected = SignatureException.class)
    public void nullData() throws SignatureException {
        signData(c1PrivateKey, null);
    }
}
