package hds.security;

import java.util.Random;

import static hds.security.CryptoUtils.hashMD5;

/**
 * Helper to generate and solve the computationally intensive challenge given to the client
 * every time it wants to interact with TransferGoodController.
 *
 * @author 		Rafael Ribeiro
 */
public class ChallengeSolver {
	private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	private static String hashedOriginalString;

	/**
	 * Tests all the String combinations with a given length consisting of the chars in the given set
	 * agains the hashedOriginalString.
	 *
	 * @param	set				All possible characters
	 * @param 	stringLength    Length of the string to discover
	 * @return 	String	 		String that was to be discovered
	 */
	private static String testAllKLength(char[] set, int stringLength) {
		int n = set.length;
		return testAllKLengthRecursive(set, "", n, stringLength);
	}

	/**
	 * Recursive method to test all combinations
	 *
	 * @param	set				All possible characters
	 * @param 	prefix			String so far
	 * @param 	n				Size of the used alphabet
	 * @param 	stringLength    Length of the string to discover
	 * @return 	String	 		String that was to be discovered
	 */
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
				return result;
			}
		}

		return "";
	}

	/**
	 * Tests a given String's hash against hashedOriginalString.
	 *
	 * @param	newString		String to test
	 * @return 	boolean	 		Whether or not the string was the wanted one
	 */
	private static boolean testNewString(String newString) {
		String hashed = hashMD5(newString);
		return hashed.equals(hashedOriginalString);
	}

	/**
	 * Solved the challenge
	 *
	 * @param	hashed			Original string's hash
	 * @param 	size			Original string's length
	 * @param 	chars			All possible characters
	 * @return 	String			String to be found
	 */
	public static String solveChallenge(String hashed, int size, char[] chars) {
		hashedOriginalString = hashed;
		return testAllKLength(chars, size);
	}

	/**
	 * Gets a random set of characters within the full alphabet
	 *
	 * @param	size			Number of chars to pick
	 * @return 	String			Full alphabet's subset
	 */
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

	/**
	 * Generates a String with a given size with elements from the given set
	 *
	 * @param 	set				All possible characters
	 * @param 	size			String's length
	 * @return 	String			String created
	 */
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
