package hds.server.exception;

/**
 * Exception to represent the connection with the database is closed.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class DBClosedConnectionException extends RuntimeException{
	public DBClosedConnectionException(String msg) {
		super(msg);
	}
}
