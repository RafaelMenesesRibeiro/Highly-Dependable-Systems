package hds.security.domain;

import java.io.Serializable;

public class BasicResponse implements Serializable {
	private final int code;
	private final String operation;
	private final String message;

	public BasicResponse(int code, String message, String operation) {
		this.code = code;
		this.message = message;
		this.operation = operation;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getOperation() {
		return operation;
	}
}
