package hds.server.exception;;

public class InvalidPropertyException extends RuntimeException{
	public InvalidPropertyException() {
		super();
	}

	public InvalidPropertyException(String msg) {
		super(msg);
	}
}
