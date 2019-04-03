package hds.security.domain;

import java.util.Base64;

public class SignedOwnerData {
	private byte[] signature;
	private OwnerData payload;

	public SignedOwnerData() {}

	public SignedOwnerData(String signature, OwnerData data) {
		this.signature = Base64.getDecoder().decode(signature);
		this.payload = data;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public OwnerData getPayload() {
		return payload;
	}

	public void setPayload(OwnerData payload) {
		this.payload = payload;
	}
}
