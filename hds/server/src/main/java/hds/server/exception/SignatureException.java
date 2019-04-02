package hds.server.exception;;

public class SignatureException extends RuntimeException {
	public SignatureException() {
		super();
	}

	public SignatureException(String msg) {
		super(msg);
	}
}
