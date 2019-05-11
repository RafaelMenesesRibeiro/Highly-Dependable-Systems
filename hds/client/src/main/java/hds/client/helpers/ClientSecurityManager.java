package hds.client.helpers;

import hds.security.msgtypes.BasicMessage;

import static hds.client.helpers.ClientProperties.print;
import static hds.client.helpers.ClientProperties.printError;
import static hds.security.SecurityManager.isValidMessage;

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

}
