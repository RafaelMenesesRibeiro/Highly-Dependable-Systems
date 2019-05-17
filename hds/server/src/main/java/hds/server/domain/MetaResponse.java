package hds.server.domain;

import hds.security.msgtypes.BasicMessage;

/**
 * Contains an HttpStatus code and a BasicMessage as payload.
 * Only used inside the Server module.
 *
 * @author 		Diogo Vilela
 * @author 		Francisco Barros
 * @author 		Rafael Ribeiro
 */
public class MetaResponse {
	private final int statusCode;
	private final BasicMessage payload;

	/**
	 * Constructor.
	 *
	 * @param 	statusCode	int representing and HttpStatus code
	 * @param 	payload		BasicMessage
	 */
	public MetaResponse(int statusCode, BasicMessage payload) {
		this.statusCode = statusCode;
		this.payload = payload;
	}

	/**
	 * Constructor. StatusCode defaults to 200 (OK).
	 *
	 * @param 	payload		BasicMessage
	 */
	public MetaResponse(BasicMessage payload) {
		this(200, payload);
	}

	/**
	 * Returns the status code.
	 *
	 * @return 	int
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns the payload.
	 *
	 * @return 	BasicMessage
	 */
	public BasicMessage getPayload() {
		return payload;
	}
}
