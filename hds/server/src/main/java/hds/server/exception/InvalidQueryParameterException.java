package hds.server.exception;;

public class InvalidQueryParameterException extends RuntimeException{
	public InvalidQueryParameterException(String msg) {
		super(msg);
	}
}
