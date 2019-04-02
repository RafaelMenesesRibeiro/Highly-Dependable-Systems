package hds.server.exception;;

public class DBConnectionRefusedException extends RuntimeException{
	public DBConnectionRefusedException () {
		super();
	}

	public DBConnectionRefusedException (String msg) {
		super(msg);
	}
}
