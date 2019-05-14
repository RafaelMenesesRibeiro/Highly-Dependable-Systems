package hds.security.ut;

import org.junit.*;

import java.nio.charset.Charset;
import java.security.SignatureException;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.*;

public class SignNVerifyUT extends BaseUT {

    @Test
    public void success() {
        try {
            String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
            Assert.assertTrue(authenticateSignatureWithPubKey(c1PublicKey, signature, testData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test
    public void wrongKeyPair() {
        try {
            String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
            Assert.assertFalse(authenticateSignatureWithPubKey(c2PublicKey, signature, testData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test
    public void wrongSignature() {
        try {
            String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
            String wrongSignature = "AAAA" + signature.substring(4);
            Assert.assertFalse(authenticateSignatureWithPubKey(c1PublicKey, wrongSignature, testData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }

    @Test
    public void wrongData() {
        try {
            String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
            Assert.assertFalse(authenticateSignatureWithPubKey(c1PublicKey, signature, wrongData));
        } catch (SignatureException se) {
            Assert.fail(se.getMessage());
        }
    }
}
