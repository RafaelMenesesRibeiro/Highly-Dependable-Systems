package hds.server.controllers.security;

import hds.server.ServerApplication;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidation {
    private static String GOOD_ID_START = "good";

    private InputValidation() {
        // This is here so the class can't be instantiated. //
    }

    public static void isValidGoodID(String str) throws IllegalArgumentException {
        isValidString(str);
        Pattern pattern = Pattern.compile("^" + GOOD_ID_START + "[0-9]+$");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The goodID " + str + " does not exist.");
        }
    }

    public static void isValidClientID(String str) throws IllegalArgumentException {
        isValidString(str);
        Pattern pattern = Pattern.compile("^[0-9]+$");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The clientID " + str + "does not exist.");
        }
        int clientID = Integer.parseInt(str);
        if (clientID <= ServerApplication.getServerPort() || clientID > ServerApplication.getMaxClientId()) {
            throw new IllegalArgumentException("The clientID " + str + "does not exist.");
        }
    }

    private static void isValidString(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("Parameter is null.");
        }
        if (str.equals("")) {
            throw new IllegalArgumentException("Parameter is empty.");
        }
        if (str.length() > 50) {
            throw new IllegalArgumentException("Parameter is longer than 50 character.");
        }
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
