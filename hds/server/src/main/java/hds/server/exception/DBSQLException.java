package hds.server.exception;;

public class DBSQLException extends RuntimeException{
	public DBSQLException () {
		super();
	}

	public DBSQLException (String msg) {
		super(msg);
	}
}
