package hds.server.msgtypes;

public class ErrorResponse extends BasicResponse {
	private final String reason;

	public ErrorResponse(String message, String operation, String reason) {
		super(message, operation);
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
