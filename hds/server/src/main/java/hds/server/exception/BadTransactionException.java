package hds.server.exception;

public class BadTransactionException extends RuntimeException {
	public BadTransactionException(String msg) {
		super(msg);
	}
}
