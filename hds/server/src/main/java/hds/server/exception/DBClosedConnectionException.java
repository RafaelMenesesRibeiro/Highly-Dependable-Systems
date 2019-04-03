package hds.server.exception;;

public class DBClosedConnectionException extends RuntimeException{
	public DBClosedConnectionException(String msg) {
		super(msg);
	}
}
