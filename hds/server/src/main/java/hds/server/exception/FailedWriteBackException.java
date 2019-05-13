package hds.server.exception;

public class FailedWriteBackException extends RuntimeException {
	public FailedWriteBackException(String msg) {
		super(msg);
	}
}
