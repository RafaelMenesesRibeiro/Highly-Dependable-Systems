package hds.server.helpers;

import hds.security.CryptoUtils;
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.lang.reflect.InvocationTargetException;

/**
 * Stores the Server's Properties
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class ServerProperties {

    private static PKCS11 pkcs11;
    private static long ccSessionID;
    private static long ccSignatureKey;

    private ServerProperties() {
        // This is here so the class can't be instantiated. //
    }

    public static PKCS11 getPKCS11() {
        return pkcs11;
    }

    public static long getCCSessionID() {
        return ccSessionID;
    }

    public static long getCCSignatureKey() {
        return ccSignatureKey;
    }

    public static void bootstrap() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, PteidException, InvocationTargetException, PKCS11Exception {
        ServerProperties.pkcs11 = CryptoUtils.getPKCS11();
        ServerProperties.ccSessionID = CryptoUtils.getCCSessionID(pkcs11);
        ServerProperties.ccSignatureKey = CryptoUtils.getCCSignatureKey(pkcs11, ccSessionID);
    }
}
