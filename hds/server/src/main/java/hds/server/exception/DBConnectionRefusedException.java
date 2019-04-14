package hds.server.exception;

/**
 * Exception to represent the connection with the database is refused.
 *
 * @author 		Rafael Ribeiro
 */
public class DBConnectionRefusedException extends RuntimeException{
	public DBConnectionRefusedException (String msg) {
		super(msg);
	}
}
