package hds.server.domain;

import hds.security.msgtypes.response.BasicResponse;

public class MetaResponse {
	private final int statusCode;
	private final BasicResponse payload;

	public MetaResponse(int statusCode, BasicResponse payload) {
		this.statusCode = statusCode;
		this.payload = payload;
	}

	public MetaResponse(BasicResponse payload) {
		this(200, payload);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public BasicResponse getPayload() {
		return payload;
	}
}
