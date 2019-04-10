package hds.server.exception;;

public class DBConnectionRefusedException extends RuntimeException{
	public DBConnectionRefusedException (String msg) {
		super(msg);
	}
}
