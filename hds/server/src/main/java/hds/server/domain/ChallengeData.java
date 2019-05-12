package hds.server.domain;

public class ChallengeData {
	// TODO - Change after testing. //
	public static final int POSSIBLE_CHAR_NUMBER = 16;
	public static final int RANDOM_STRING_LENGTH = 2;

	private final String requestID;
	private final String originalString;
	private final String hashedOriginalString;
	private final char[] alphabet;

	public ChallengeData(String requestID, String originalString, String hashedOriginalString, char[] alphabet) {
		this.requestID = requestID;
		this.originalString = originalString;
		this.hashedOriginalString = hashedOriginalString;
		this.alphabet = alphabet;
	}

	public String getRequestID() {
		return requestID;
	}

	public String getOriginalString() {
		return originalString;
	}

	public String getHashedOriginalString() {
		return hashedOriginalString;
	}

	public char[] getAlphabet() {
		return alphabet;
	}

	public boolean verify(String challengeResponse) {
		return challengeResponse.equals(originalString);
	}
}
