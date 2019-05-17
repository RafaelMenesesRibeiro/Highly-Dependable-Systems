package hds.security.ut;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.security.SignatureException;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.authenticateSignatureWithPubKey;
import static hds.security.CryptoUtils.signData;

public class VerifyUT extends BaseUT {

    private static String signature;

    @Before
    public void sign() throws Exception {
        signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
    }

    @Test
    public void success() {
        try {
            Assert.assertTrue(authenticateSignatureWithPubKey(c1PublicKey, signature, testData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test
    public void wrongKeyPair() {
        try {
            Assert.assertFalse(authenticateSignatureWithPubKey(c2PublicKey, signature, testData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test
    public void wrongSignature() {
        try {
            String wrongSignature = "AAAA" + signature.substring(4);
            Assert.assertFalse(authenticateSignatureWithPubKey(c1PublicKey, wrongSignature, testData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test
    public void wrongData() {
        try {
            Assert.assertFalse(authenticateSignatureWithPubKey(c1PublicKey, signature, wrongData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test(expected = SignatureException.class)
    public void nullPublicKey() throws SignatureException {
        authenticateSignatureWithPubKey(null, signature, testData);
    }

    @Test(expected = SignatureException.class)
    public void nullSignature() throws SignatureException {
        authenticateSignatureWithPubKey(c1PublicKey, null, testData);
    }

    @Test(expected = SignatureException.class)
    public void nullTestData() throws SignatureException {
        authenticateSignatureWithPubKey(c1PublicKey, signature, null);
    }

    @Test(expected = SignatureException.class)
    public void malformedSignature() throws SignatureException {
        authenticateSignatureWithPubKey(c1PublicKey, malformedSignature, testData);
    }
}
