package hds.server.exception;

/**
 * Exception to represent the database query provided no results.
 * Usually because a GoodID or BuyerID/SellerID do not exist in the database.
 *
 * @author 		Rafael Ribeiro
 */
public class DBNoResultsException extends RuntimeException{
	public DBNoResultsException (String msg) {
		super(msg);
	}
}
