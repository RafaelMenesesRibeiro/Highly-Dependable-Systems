package hds.security.helpers;

import java.util.ArrayList;
import java.util.Random;

// TODO - Javadoc. //
public class StringHelper {
	private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	static void getAllKLength(char[] set, int stringLength, ArrayList<String> combnations) {
		int n = set.length;
		getAllKLengthRecursive(combnations, set, "", n, stringLength);
	}

	static void getAllKLengthRecursive(ArrayList<String> combinations, char[] set, String prefix, int n, int stringLegnth) {
		if (stringLegnth == 0) {
			combinations.add(prefix);
			return;
		}

		for (int i = 0; i < n; ++i) {
			String newPrefix = prefix + set[i];
			getAllKLengthRecursive(combinations, set, newPrefix, n, stringLegnth - 1);
		}
	}

	public static ArrayList<String> getAllCombinations(char[] chars, int size) {
		ArrayList<String> combinations = new ArrayList<>();
		getAllKLength(chars, size, combinations);
		return combinations;
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
