package hds.server.exception;

/**
 * Exception to represent a bad transaction.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class BadTransactionException extends RuntimeException {
	public BadTransactionException(String msg) {
		super(msg);
	}
}
