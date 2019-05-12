package hds.server.domain;

public class ChallengeData {
	private final String requestID;
	// TODO - Change //
	private final int changeWhenDecided;

	public ChallengeData(String requestID, int changeWhenDecided) {
		this.requestID = requestID;
		this.changeWhenDecided = changeWhenDecided;
	}

	public String getRequestID() {
		return requestID;
	}

	public int getChangeWhenDecided() {
		return changeWhenDecided;
	}
}
