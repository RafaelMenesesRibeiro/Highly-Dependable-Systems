package hds.security.msgtypes.responses;

import hds.security.exceptions.SignatureException;

import static hds.security.SecurityManager.*;

public class SecureResponse {
	private BasicResponse payload;
	private String signature;

	public SecureResponse() {

	}

	public SecureResponse(BasicResponse response, String b64signature) {
		this.payload = response;
		this.signature = b64signature;
	}

	public SecureResponse(BasicResponse response) {
		try {
			this.payload = response;
			this.signature = bytesToBase64String(signData(getByteArray(response)));
		}
		catch (Exception e) {
			throw new SignatureException(e.getMessage());
		}
	}

	public BasicResponse getPayload() {
		return payload;
	}

	public void setPayload(BasicResponse payload) {
		this.payload = payload;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
		return "SecureResponse{" +
				"payload=" + payload +
				", signature='" + signature + '\'' +
				'}';
	}
}
