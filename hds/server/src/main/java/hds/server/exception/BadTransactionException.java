package hds.server.exception;

/**
 * Exception to represent a bad transaction.
 *
 * @author 		Rafael Ribeiro
 */
public class BadTransactionException extends RuntimeException {
	public BadTransactionException(String msg) {
		super(msg);
	}
}
