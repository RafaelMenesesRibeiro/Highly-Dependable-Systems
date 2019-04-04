package hds.security.msgtypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse extends BasicResponse {
	private String reason;

	@JsonCreator
	public ErrorResponse(@JsonProperty("message") String message,
						 @JsonProperty("operation") String operation,
						 String reason) {

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
