package hds.server.exception;;

public class IncorrectSignatureException extends RuntimeException {
	public IncorrectSignatureException() {
		super();
	}

	public IncorrectSignatureException(String msg) {
		super(msg);
	}
}
