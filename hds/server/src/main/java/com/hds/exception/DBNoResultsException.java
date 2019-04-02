package com.hds.exception;

public class DBNoResultsException extends RuntimeException{
	public DBNoResultsException () {
		super();
	}

	public DBNoResultsException (String msg) {
		super(msg);
	}
}
