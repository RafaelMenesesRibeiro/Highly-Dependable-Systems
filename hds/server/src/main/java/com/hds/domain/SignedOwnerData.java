package com.hds.domain;

import java.util.Base64;

public class SignedOwnerData {
	private byte[] signature;
	private OwnerData payload;

	public SignedOwnerData(String signature, OwnerData data) {
		this.signature = Base64.getDecoder().decode(signature);
		this.payload = data;
	}

	public byte[] getSignature() {
		return signature;
	}

	public OwnerData getPayload() {
		return payload;
	}
}
