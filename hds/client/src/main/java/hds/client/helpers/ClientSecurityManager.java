package hds.client.helpers;

import hds.security.CryptoUtils;
import hds.security.msgtypes.BasicMessage;
import org.json.JSONException;

import java.security.SignatureException;

import static hds.client.helpers.ClientProperties.*;
import static hds.client.helpers.ClientProperties.getPrivateKey;
import static hds.security.SecurityManager.*;

public class ClientSecurityManager {

    public static boolean isMessageFreshAndAuthentic(BasicMessage responseMessage) {
        if (responseMessage == null) {
            return false;
        }
        // Verify freshness and authenticity using isValidMessage
        String validityString = isValidMessage(responseMessage);
        if (!"".equals(validityString)) {
            // Non-empty string means something went wrong during validation. Message isn't fresh or isn't properly signed
            printError(validityString);
            return false;
        }
        else {
            // Everything is has expected
            print(responseMessage.toString());
            return true;
        }
    }

    public static byte[] newWriteOnGoodsDataSignature(final String goodId,
                                                      final Boolean onSale,
                                                      final String writer,
                                                      final long wts) throws JSONException, SignatureException {

        byte[] rawData = newWriteOnGoodsData(goodId, onSale, writer, wts).toString().getBytes();
        return CryptoUtils.signData(getPrivateKey(), rawData);
    }

    public static byte[] newWriteOnOwnershipsDataSignature(final String goodId,
                                                           final String writerID,
                                                           final long wts) throws JSONException, SignatureException {

        byte[] rawData = newWriteOnOwnershipData(goodId, writerID, wts).toString().getBytes();
        return CryptoUtils.signData(getPrivateKey(), rawData);
    }
}