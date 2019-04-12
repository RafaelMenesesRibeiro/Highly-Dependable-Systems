package hds.security.helpers;

public class ControllerErrorConsts {

	private ControllerErrorConsts() {
		// This is here so the class can't be instantiated. //
	}

	public static final String CRASH = "The server cannot continue.";
	public static final String BAD_PARAMS = "The parameters are not correct.";
	public static final String BAD_TRANSACTION = "The transaction is not valid.";
	public static final String BAD_SIGNATURE = "The signatures do not match the received data.";
	public static final String BAD_SQL = "Caught an SQL Exception.";

	public static final String NO_PERMISSION = "You do not have permission to put this item on sale.";
	public static final String OLD_MESSAGE = "This request has expired.";

	public static final String CONN_REF = "The connection to the database was refused.";
	public static final String CONN_CLOSED = "The connection to the database was closed.";

	public static final String NO_RESP = "The database did not return a responses for the query.";
}
