package com.hds.server.msgtypes;

import com.hds.helpers.SecurityManager;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class SecureResponse {
	private BasicResponse payload;
	private byte[] signature;

	public SecureResponse(BasicResponse response, byte[] signature) {
		this.payload = response;
		this.signature = signature;
	}

	public SecureResponse(BasicResponse response) {
		try {
			byte[] data = SecurityManager.getByteArray(response);
			this.payload = response;
			this.signature = SecurityManager.signData(data);
		}
		catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException | InvalidKeySpecException e) {
			throw new com.hds.exception.SignatureException(e.getMessage());
		}
	}

	public SecureResponse(BasicResponse response, boolean isException) {
		this.payload = response;
		this.signature = new byte[0];
	}

	public BasicResponse getPayload() {
		return payload;
	}

	public byte[] getSignature() {
		return signature;
	}
}
