package hds.security.msgtypes;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

public class ChallengeRequestResponse extends BasicMessage {
	@NotNull(message = "The hashed original string cannot be null.")
	@NotEmpty(message = "The hashed original string cannot be empty.")
	private String hashedOriginalString;

	@NotNull(message = "The alphabet subset cannot be null.")
	@NotEmpty(message = "The alphabet subset cannot be empty.")
	private char[] alphabet;

	@NotNull(message = "The original string size cannot be null.")
	private int originalStringSize;

	public ChallengeRequestResponse(long timestamp,
									String requestID,
									String operation,
									String from,
									String to,
									String signature,
									String hashedOriginalString,
									char[] alphabet,
									int originalStringSize) {
		super(timestamp, requestID, operation, from, to, signature);
		this.hashedOriginalString = hashedOriginalString;
		this.alphabet = alphabet;
		this.originalStringSize = originalStringSize;
	}

	public ChallengeRequestResponse() { }

	public String getHashedOriginalString() {
		return hashedOriginalString;
	}

	public void setHashedOriginalString(String hashedOriginalString) {
		this.hashedOriginalString = hashedOriginalString;
	}

	public char[] getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(char[] alphabet) {
		this.alphabet = alphabet;
	}

	public int getOriginalStringSize() {
		return originalStringSize;
	}

	public void setOriginalStringSize(int stringSize) {
		this.originalStringSize = stringSize;
	}

	@Override
	public String toString() {
		return "ChallengeRequestResponse{" +
				"hashedOriginalString='" + hashedOriginalString + '\'' +
				", alphabet=" + Arrays.toString(alphabet) +
				", originalStringSize=" + originalStringSize +
				", requestID='" + requestID + '\'' +
				", operation='" + operation + '\'' +
				", from='" + from + '\'' +
				", to='" + to + '\'' +
				", signature='" + signature + '\'' +
				'}';
	}
}
