package hds.server.exception;

/**
 * Exception to client has no permission for the transaction it is trying.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class NoPermissionException extends RuntimeException {
	public NoPermissionException(String msg) {
		super(msg);
	}
}
