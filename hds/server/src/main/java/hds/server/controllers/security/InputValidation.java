package hds.server.controllers.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class InputValidation {
    private InputValidation() {
        // This is here so the class can't be instantiated. //
    }

    public static String cleanString(String str) {
        if (str == null) {
            return "";
        }
        else {
            return Jsoup.clean(str, Whitelist.simpleText());
        }
    }
}
