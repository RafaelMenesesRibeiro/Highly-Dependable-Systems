package hds.server.exception;

/**
 * Exception to represent the connection with the database is refused.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class DBConnectionRefusedException extends RuntimeException{
	public DBConnectionRefusedException (String msg) {
		super(msg);
	}
}
