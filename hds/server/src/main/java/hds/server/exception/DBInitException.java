package hds.server.exception;

/**
 * Exception to represent the creation / population of a database was not successful.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class DBInitException extends RuntimeException{

	public DBInitException (String msg) {
		super(msg);
	}
}
