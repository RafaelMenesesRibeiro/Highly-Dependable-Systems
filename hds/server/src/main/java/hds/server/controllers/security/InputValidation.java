package hds.server.controllers.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Responsible for sanitizing input Strings with the Jsoup library.
 *
 * @author 		Rafael Ribeiro
 */
public class InputValidation {
    private InputValidation() {
        // This is here so the class can't be instantiated. //
    }

    /**
     * Cleans a string with the Jsoup library.
     *
     * @param   str     String to clean
     * @return  String  Cleaned String
     * @see     Jsoup
     * @see     Whitelist
     */
    public static String cleanString(String str) {
        if (str == null) {
            return "";
        }
        else {
            return Jsoup.clean(str, Whitelist.simpleText());
        }
    }
}
