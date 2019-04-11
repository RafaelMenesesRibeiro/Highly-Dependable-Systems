package hds.security;

import hds.security.msgtypes.BasicMessage;

import java.security.PrivateKey;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.ConvertUtils.objectToByteArray;
import static hds.security.CryptoUtils.signData;

public class SecurityManager {

    public static BasicMessage setMessageSignature(PrivateKey privateKey, BasicMessage message) throws Exception {
        message.setSignature(bytesToBase64String(signData(privateKey, objectToByteArray(message))));
        return message;
    }
}
