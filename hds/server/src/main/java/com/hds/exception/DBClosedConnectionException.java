package com.hds.exception;

public class DBClosedConnectionException extends RuntimeException{
	public DBClosedConnectionException() {
		super();
	}

	public DBClosedConnectionException(String msg) {
		super(msg);
	}
}
