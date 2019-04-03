package hds.security.msgtypes;

public class ErrorResponse extends BasicResponse {
	private String reason;

	public ErrorResponse(String message, String operation, String reason) {
		super(message, operation);
		this.reason = reason;
	}

	public ErrorResponse() {
		// This is here so the class can't be instantiated. //
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
