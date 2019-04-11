package hds.security;

import hds.security.msgtypes.BasicMessage;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class CryptoUtils {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /***********************************************************
     *
     * PUBLIC METHODS RELATED WITH SIGNATURE CREATION
     *
     ***********************************************************/

    static byte[] signData(PrivateKey key, byte[] data) throws SignatureException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initSign(key);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException exc) {
            throw new SignatureException(exc.getMessage());
        }
    }

    static byte[] signData(PKCS11 pkcs11, long ccSessionID, long ccSignatureKey, byte[] data) throws SignatureException {
        try {
            pkcs11.C_FindObjectsFinal(ccSessionID);
            CK_MECHANISM mechanism = new CK_MECHANISM();
            mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
            mechanism.pParameter = null;
            pkcs11.C_SignInit(ccSessionID, mechanism, ccSignatureKey);
            return pkcs11.C_Sign(ccSessionID, data);
        } catch (PKCS11Exception exc) {
            throw new SignatureException(exc.getMessage());
        }
    }

    /***********************************************************
     *
     * PUBLIC METHODS RELATED WITH SIGNATURE VERIFICATION
     *
     ***********************************************************/

    public static boolean authenticateSignature(PublicKey key, String signedData, Object payload) throws IOException, SignatureException {
        return authenticateSignature(key, ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
    }

    public static boolean authenticateSignatureWithCert(X509Certificate certificate, String signedData, Object payload)
            throws IOException, CertificateException, SignatureException {
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException exc) {
            throw new CertificateException(exc.getMessage());
        }
        return authenticateSignature(certificate.getPublicKey(), ConvertUtils.base64StringToBytes(signedData), ConvertUtils.objectToByteArray(payload));
    }

    /***********************************************************
     *
     * PRIVATE METHODS RELATED WITH SIGNATURE VERIFICATION
     *
     ***********************************************************/

    private static boolean authenticateSignature(PublicKey key, byte[] signedData, byte[] testData) throws SignatureException {
        try {
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initVerify(key);
            sign.update(testData);
            return sign.verify(signedData);
        } catch (NoSuchAlgorithmException | InvalidKeyException exc) {
            throw new SignatureException(exc.getMessage());
        }
    }

    /***********************************************************
     *
     * PUBLIC METHODS RELATED WITH SERVER BOOTSTRAP
     *
     ***********************************************************/

    public static PKCS11 getPKCS11() throws PteidException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.loadLibrary("pteidlibj");
        pteid.Init(""); // Initializes the eID Lib
        pteid.SetSODChecking(false);

        String osName = System.getProperty("os.name");
        String libName = "libpteidpkcs11.so";

        if (osName.contains("Windows"))
            libName = "pteidpkcs11.dll";
        else if (osName.contains("Mac"))
            libName = "pteidpkcs11.dylib";
        Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        Method getInstanceMethod = pkcs11Class.getDeclaredMethod("getInstance", String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class);
        return (PKCS11)getInstanceMethod.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });

    }

    public static long getCCSessionID(PKCS11 pkcs11) throws PKCS11Exception {
        return pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
    }

    public static long getCCSignatureKey(PKCS11 pkcs11, long ccSessionID) throws PKCS11Exception {
        pkcs11.C_Login(ccSessionID, 1, null);
        CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(ccSessionID);
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = PKCS11Constants.CKO_PRIVATE_KEY;
        pkcs11.C_FindObjectsInit(ccSessionID, attributes);
        long[] keyHandles = pkcs11.C_FindObjects(ccSessionID, 5);
        return keyHandles[0];
    }

    /***********************************************************
     *
     * HELPER METHODS NOT EXPLICITLY RELATED WITH SECURITY
     *
     ***********************************************************/

    public static String newUUIDString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
