package hds.security.domain;

import hds.security.SecurityManager;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.Logger;

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
		catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException | InvalidKeySpecException e) {
			// TODO - Remove this. //
			Logger logger = Logger.getAnonymousLogger();
			logger.warning(e.toString());
			logger.warning(Arrays.toString(e.getStackTrace()));
			logger.warning("Caught exception: " + e.getMessage());
		}
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
