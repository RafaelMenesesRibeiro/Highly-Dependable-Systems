package hds.security.domain;

import java.util.Base64;

public class SignedTransactionData {

	private TransactionData payload;
	private byte[] buyerSignature;
	private byte[] sellerSignature;

	public SignedTransactionData(TransactionData data, String buyerSignature, String sellerSignature) {
		this.payload = data;
		this.buyerSignature = Base64.getDecoder().decode(buyerSignature);
		this.sellerSignature = Base64.getDecoder().decode(sellerSignature);
	}

	public TransactionData getPayload() {
		return payload;
	}

	public byte[] getBuyerSignature() {
		return buyerSignature;
	}

	public byte[] getSellerSignature() {
		return sellerSignature;
	}

	public void setSellerSignature(byte[] sellerSignature) {
	    this.sellerSignature = sellerSignature;
    }
}
