package hds.server.exception;

/**
 * Exception to represent the signature of a BasicMessage does not match its contents.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class IncorrectSignatureException extends RuntimeException {
	public IncorrectSignatureException(String msg) {
		super(msg);
	}
}
