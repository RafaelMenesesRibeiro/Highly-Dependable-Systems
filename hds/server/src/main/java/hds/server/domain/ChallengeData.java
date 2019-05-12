package hds.server.domain;

public class ChallengeData {
	public static final int POSSIBLE_CHAR_NUMBER = 16;
	public static final int RANDOM_STRING_LENGHT = 5;

	private final String requestID;
	private final String randomStringToFind;
	private final String hashedRandomString;
	private final char[] alphabet;
	private final int randomStringSize;

	public ChallengeData(String requestID, String randomStringToFind, String hashedRandomString, char[] alphabet) {
		this.requestID = requestID;
		this.randomStringToFind = randomStringToFind;
		this.hashedRandomString = hashedRandomString;
		this.alphabet = alphabet;
		this.randomStringSize = randomStringToFind.length();
	}

	public String getRequestID() {
		return requestID;
	}

	public String getRandomStringToFind() {
		return randomStringToFind;
	}

	public String getHashedRandomString() {
		return hashedRandomString;
	}

	public char[] getAlphabet() {
		return alphabet;
	}

	public int getRandomStringSize() {
		return randomStringSize;
	}
}
