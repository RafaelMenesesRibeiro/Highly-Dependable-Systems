package hds.security.msgtypes;

public class ChallengeRequestResponse extends BasicMessage {
	// TODO - Add validation annotations
	private int changeWhenDecided;

	public ChallengeRequestResponse(long timestamp,
									String requestID,
									String operation,
									String from,
									String to,
									String signature,
									int changeWhenDecided) {
		super(timestamp, requestID, operation, from, to, signature);
		this.changeWhenDecided = changeWhenDecided;
	}

	public ChallengeRequestResponse() { }

	public int getChangeWhenDecided() {
		return changeWhenDecided;
	}

	public void setChangeWhenDecided(int changeWhenDecided) {
		this.changeWhenDecided = changeWhenDecided;
	}

	@Override
	public String toString() {
		return "ChallengeRequestResponse{" +
				"changeWhenDecided=" + changeWhenDecided +
				", requestID='" + requestID + '\'' +
				", operation='" + operation + '\'' +
				", from='" + from + '\'' +
				", to='" + to + '\'' +
				", signature='" + signature + '\'' +
				'}';
	}
}
