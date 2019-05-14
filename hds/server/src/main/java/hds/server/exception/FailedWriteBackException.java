package hds.server.exception;

/**
 * Exception to represent the write back operation is not valid / failed.
 * Usually because the given write timestamp is inferior to the replica's write timestamp.
 *
 * @author 		Rafael Ribeiro
 */
public class FailedWriteBackException extends RuntimeException {
	public FailedWriteBackException(String msg) {
		super(msg);
	}
}
