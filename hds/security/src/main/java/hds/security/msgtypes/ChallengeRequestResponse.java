package hds.security.msgtypes;

import java.util.Arrays;

public class ChallengeRequestResponse extends BasicMessage {
	private String hashedRandomString;
	private char[] alphabet;
	private int stringSize;

	public ChallengeRequestResponse(long timestamp,
									String requestID,
									String operation,
									String from,
									String to,
									String signature,
									String hashedRandomString,
									char[] alphabet,
									int stringSize) {
		super(timestamp, requestID, operation, from, to, signature);
		this.hashedRandomString = hashedRandomString;
		this.alphabet = alphabet;
		this.stringSize = stringSize;
	}

	public ChallengeRequestResponse() { }

	public String getHashedRandomString() {
		return hashedRandomString;
	}

	public void setHashedRandomString(String hashedRandomString) {
		this.hashedRandomString = hashedRandomString;
	}

	public char[] getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(char[] alphabet) {
		this.alphabet = alphabet;
	}

	public int getStringSize() {
		return stringSize;
	}

	public void setStringSize(int stringSize) {
		this.stringSize = stringSize;
	}

	@Override
	public String toString() {
		return "ChallengeRequestResponse{" +
				"hashedRandomString='" + hashedRandomString + '\'' +
				", alphabet=" + Arrays.toString(alphabet) +
				", stringSize=" + stringSize +
				", requestID='" + requestID + '\'' +
				", operation='" + operation + '\'' +
				", from='" + from + '\'' +
				", to='" + to + '\'' +
				", signature='" + signature + '\'' +
				'}';
	}
}
