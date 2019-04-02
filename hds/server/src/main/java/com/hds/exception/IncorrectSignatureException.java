package com.hds.exception;

public class IncorrectSignatureException extends RuntimeException {
	public IncorrectSignatureException() {
		super();
	}

	public IncorrectSignatureException(String msg) {
		super(msg);
	}
}
