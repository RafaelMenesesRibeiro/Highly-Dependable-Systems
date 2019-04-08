package hds.server.helpers;

import hds.server.exception.InvalidStringException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputProcessor {

    private InputProcessor() {
        // This is here so the class can't be instantiated. //
    }

    public static void isValidString(String str) throws InvalidStringException {
        if (str == null) {
            throw new InvalidStringException("Parameter is null.");
        }
        if (str.equals("")) {
            throw new InvalidStringException("Parameter is empty.");
        }
        if (!isAlphanumericString(str)) {
            throw new InvalidStringException("Parameter is not alphanumeric.");
        }
        if (str.length() > 50) {
            throw new InvalidStringException("Parameter is longer than 50 character.");
        }
    }

    private static boolean isAlphanumericString(String catalogTitle) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
        Matcher matcher = pattern.matcher(catalogTitle);
        return matcher.matches();
    }
}
