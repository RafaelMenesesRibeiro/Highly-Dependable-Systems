package hds.security.domain;

public class ErrorResponse extends BasicResponse {
	private final String reason;

	public ErrorResponse(int code, String message, String operation, String reason) {
		super(code, message, operation);
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
