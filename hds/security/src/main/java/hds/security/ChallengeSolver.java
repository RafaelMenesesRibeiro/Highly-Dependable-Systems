package hds.security;

import java.util.Random;

import static hds.security.CryptoUtils.hashMD5;

// TODO - Javadoc. //
public class ChallengeSolver {
	private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	private static String hashedOriginalString;

	private static String testAllKLength(char[] set, int stringLength) {
		int n = set.length;
		return testAllKLengthRecursive(set, "", n, stringLength);
	}

	private static String testAllKLengthRecursive(char[] set, String prefix, int n, int stringLength) {
		if (stringLength == 0) {
			if (testNewString(prefix)) {
				return prefix;
			}
			return "";
		}

		for (int i = 0; i < n; ++i) {
			String newPrefix = prefix + set[i];
			String result = testAllKLengthRecursive(set, newPrefix, n, stringLength - 1);
			if (!result.equals("")) {
				return newPrefix;
			}
		}

		return "";
	}

	private static boolean testNewString(String newString) {
		String hashed = hashMD5(newString);
		return hashed.equals(hashedOriginalString);
	}

	// TODO - Use this somewhere. //
	public static String solveChallenge(String hashed, int size, char[] chars) {
		hashedOriginalString = hashed;
		return testAllKLength(chars, size);
	}

	public static char[] getRandomAlphabetSet(int size) {
		if (size >= ALPHABET.length) {
			return ALPHABET;
		}

		Random randomG = new Random();
		int alphabetSize = ALPHABET.length;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < size; i++) {
			int index = randomG.nextInt(alphabetSize);
			char chosen = ALPHABET[index];
			while (result.toString().contains(""+chosen)) {
				index = randomG.nextInt(alphabetSize);
				chosen = ALPHABET[index];
			}
			result.append(chosen);
		}
		return result.toString().toCharArray();
	}

	public static String generateFromSet(char[] set, int size) {
		Random randomG = new Random();
		int setSize = set.length;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < size; i++) {
			result.append(set[randomG.nextInt(setSize)]);
		}
		return result.toString();
	}
}
