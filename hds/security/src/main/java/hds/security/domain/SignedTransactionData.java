package hds.security.domain;

import java.io.Serializable;

public class SignedTransactionData implements Serializable {
	private TransactionData payload;
	private String buyerSignature;
	private String sellerSignature;

	public SignedTransactionData() {}

	public SignedTransactionData(TransactionData data, String buyerSignature, String sellerSignature) {
		this.payload = data;
		this.buyerSignature = buyerSignature;
		this.sellerSignature = sellerSignature;
	}

	public TransactionData getPayload() {
		return payload;
	}

	public void setPayload(TransactionData payload) {
		this.payload = payload;
	}

	public String getBuyerSignature() {
		return buyerSignature;
	}

	public void setBuyerSignature(String buyerSignature) {
		this.buyerSignature = buyerSignature;
	}

	public String getSellerSignature() {
		return sellerSignature;
	}

	public void setSellerSignature(String sellerSignature) {
		this.sellerSignature = sellerSignature;
	}

	@Override
	public String toString() {
		return "SignedTransactionData{" +
				"payload=" + payload +
				", buyerSignature='" + buyerSignature + '\'' +
				", sellerSignature='" + sellerSignature + '\'' +
				'}';
	}
}
