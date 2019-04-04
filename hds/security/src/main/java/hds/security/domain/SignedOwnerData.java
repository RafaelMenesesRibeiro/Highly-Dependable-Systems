package hds.security.domain;

import java.io.Serializable;

public class SignedOwnerData implements Serializable {
	private OwnerData payload;
	private String signature;

	public SignedOwnerData() {}

	public SignedOwnerData(OwnerData data, String signature) {
		this.payload = data;
		this.signature = signature;
	}

	public OwnerData getPayload() {
		return payload;
	}

	public void setPayload(OwnerData payload) {
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
		return "SignedOwnerData{" +
				"signature='" + signature + '\'' +
				", payload=" + payload +
				'}';
	}
}
