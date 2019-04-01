package hds.security.domain;

import java.io.Serializable;

public class BasicResponse implements Serializable {
	private int code;
	private String operation;
	private String message;

	public BasicResponse(int code, String message, String operation) {
		this.code = code;
		this.message = message;
		this.operation = operation;
	}

	public BasicResponse() {


	}
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
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
				"code=" + code +
				", operation='" + operation + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}
