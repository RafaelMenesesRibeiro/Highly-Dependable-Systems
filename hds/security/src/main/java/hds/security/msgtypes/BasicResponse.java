package hds.security.msgtypes;

import java.io.Serializable;

public class BasicResponse implements Serializable {
	private String operation;
	private String message;

	public BasicResponse(String message, String operation) {
		this.message = message;
		this.operation = operation;
	}

	public BasicResponse() {
		// This is here so the class can't be instantiated. //
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "BasicResponse{" +
				", operation='" + operation + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}
