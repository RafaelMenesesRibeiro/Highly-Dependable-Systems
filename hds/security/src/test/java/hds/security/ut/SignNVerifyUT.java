package hds.security.ut;

import org.junit.*;

import java.nio.charset.Charset;
import java.security.SignatureException;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.CryptoUtils.*;

public class SignNVerifyUT extends BaseUT {

    private static String testData = "TEST";
    private static String wrongData = "WRONG_TEST";

    @Test
    public void signNVerifySuccess() throws SignatureException {
        String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
        Assert.assertTrue(authenticateSignatureWithPubKey(c1PublicKey, signature, testData));
    }

    @Test
    public void signNVerifyFailWrongKeyPair() throws SignatureException {
        String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
        Assert.assertFalse(authenticateSignatureWithPubKey(c2PublicKey, signature, testData));
    }

    @Test
    public void signNVerifyFailWrongSignature() throws SignatureException {
        String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
        String wrongSignature = "AAAA" + signature.substring(4);
        Assert.assertFalse(authenticateSignatureWithPubKey(c1PublicKey, wrongSignature, testData));
    }

    @Test
    public void signNVerifyFailWrongData() throws SignatureException {
        String signature = bytesToBase64String(signData(c1PrivateKey, testData.getBytes(Charset.forName("UTF-8"))));
        Assert.assertFalse(authenticateSignatureWithPubKey(c1PublicKey, signature, wrongData));
    }
}
