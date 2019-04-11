package hds.server.exception;

public class DBNoResultsException extends RuntimeException{
	public DBNoResultsException (String msg) {
		super(msg);
	}
}
