package hds.security.msgtypes.response;

import hds.security.SecurityManager;
import hds.security.exceptions.SignatureException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class SecureResponse {
	private BasicResponse payload;
	private byte[] signature;

	public SecureResponse(BasicResponse response, byte[] signature) {
		this.payload = response;
		this.signature = signature;
	}

	public SecureResponse() {

	}

	public SecureResponse(BasicResponse response) {
		try {
			byte[] data = SecurityManager.getByteArray(response);
			this.payload = response;
			this.signature = SecurityManager.signData(data);
		}
		catch (NoSuchAlgorithmException | InvalidKeyException | java.security.SignatureException | IOException | InvalidKeySpecException e) {
				throw new SignatureException(e.getMessage());
			}
	}

	public SecureResponse(BasicResponse response, boolean isException) {
		this.payload = response;
		this.signature = new byte[0];
	}

	public BasicResponse getPayload() {
		return payload;
	}

	public void setPayload(BasicResponse payload) {
		this.payload = payload;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
		return "SecureResponse{" +
				"payload=" + payload.toString() +
				", signature=" + Arrays.toString(signature) +
				'}';
	}
}
