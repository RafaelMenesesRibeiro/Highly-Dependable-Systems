package hds.server.exception;

public class InvalidStringException extends RuntimeException {
	public InvalidStringException() {
		super();
	}

	public InvalidStringException(String msg) {
		super(msg);
	}
}
