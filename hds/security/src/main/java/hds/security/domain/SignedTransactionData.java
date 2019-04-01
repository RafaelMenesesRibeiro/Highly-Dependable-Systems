package hds.security.domain;

import java.util.Base64;

public class SignedTransactionData {
	private TransactionData payload;
	private byte[] buyerSignature;
	private byte[] sellerSignature;

	public SignedTransactionData(TransactionData data, String buyerSignature) {
		this.payload = data;
		this.buyerSignature = Base64.getDecoder().decode(buyerSignature);
	}

	public TransactionData getPayload() {
		return payload;
	}

	public void setPayload(TransactionData payload) {
		this.payload = payload;
	}

	public byte[] getBuyerSignature() {
		return buyerSignature;
	}

	public void setBuyerSignature(byte[] buyerSignature) {
		this.buyerSignature = buyerSignature;
	}

	public byte[] getSellerSignature() {
		return sellerSignature;
	}

	public void setSellerSignature(byte[] sellerSignature) {
		this.sellerSignature = sellerSignature;
	}
}
