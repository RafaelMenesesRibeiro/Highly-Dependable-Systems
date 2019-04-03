package hds.server.msgtypes;

import java.io.Serializable;

public class BasicResponse implements Serializable {
	private final String operation;
	private final String message;

	public BasicResponse(String message, String operation) {
		this.message = message;
		this.operation = operation;
	}

	public String getMessage() {
		return message;
	}

	public String getOperation() {
		return operation;
	}
}
