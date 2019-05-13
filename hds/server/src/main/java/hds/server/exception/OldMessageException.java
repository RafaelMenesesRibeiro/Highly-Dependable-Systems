package hds.server.exception;

/**
 * Some of the request's timestamps are too old to be relevant.
 *
 * @author 		Rafael Ribeiro
 */
public class OldMessageException extends RuntimeException{
	public OldMessageException(String msg) {
		super(msg);
	}
}
