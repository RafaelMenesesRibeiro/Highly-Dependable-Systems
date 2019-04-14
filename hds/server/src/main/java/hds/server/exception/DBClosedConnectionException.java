package hds.server.exception;

/**
 * Exception to represent the connection with the database is closed.
 *
 * @author 		Rafael Ribeiro
 */
public class DBClosedConnectionException extends RuntimeException{
	public DBClosedConnectionException(String msg) {
		super(msg);
	}
}
