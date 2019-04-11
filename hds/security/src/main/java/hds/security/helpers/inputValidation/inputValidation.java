package hds.security.helpers.inputValidation;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class inputValidation {
	public static String cleanString(String str) {
		if (str == null) {
			return "";
		}
		else {
			return Jsoup.clean(str, Whitelist.simpleText());
		}
	}
}
