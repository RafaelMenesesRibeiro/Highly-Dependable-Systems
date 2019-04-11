package hds.server.domain;

import hds.security.msgtypes.BasicMessage;

public class MetaResponse {
	private final int statusCode;
	private final BasicMessage payload;

	public MetaResponse(int statusCode, BasicMessage payload) {
		this.statusCode = statusCode;
		this.payload = payload;
	}

	public MetaResponse(BasicMessage payload) {
		this(200, payload);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public BasicMessage getPayload() {
		return payload;
	}
}
