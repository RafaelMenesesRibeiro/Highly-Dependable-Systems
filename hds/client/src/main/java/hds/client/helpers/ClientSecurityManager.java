package hds.client.helpers;

import hds.security.CryptoUtils;
import hds.security.msgtypes.BasicMessage;
import org.json.JSONException;

import java.security.SignatureException;

import static hds.client.helpers.ClientProperties.getMyPrivateKey;
import static hds.client.helpers.ClientProperties.printError;
import static hds.security.SecurityManager.*;

public class ClientSecurityManager {


    public static boolean isMessageFreshAndAuthentic(BasicMessage message) {
        // Verify freshness and authenticity using isValidMessage
        String validityString = isValidMessage(message);
        if (!"".equals(validityString)) {
            // Non-empty string means something went wrong during validation. Message isn't fresh or isn't properly signed
            printError(validityString);
            return false;
        }
        else {
            // Everything is has expected
            return true;
        }
    }

    public static byte[] newWriteOnGoodsDataSignature(final String goodId,
                                                      final Boolean onSale,
                                                      final String writer,
                                                      final int wts) throws JSONException, SignatureException {

        byte[] rawData = newWriteOnGoodsData(goodId, onSale, writer, wts).toString().getBytes();
        return CryptoUtils.signData(getMyPrivateKey(), rawData);
    }

    public static byte[] newWriteOnOwnershipsDataSignature(final String goodId,
                                                           final String writerID,
                                                           final int wts) throws JSONException, SignatureException {

        byte[] rawData = newWriteOnOwnershipData(goodId, writerID, wts).toString().getBytes();
        return CryptoUtils.signData(getMyPrivateKey(), rawData);
    }
}
