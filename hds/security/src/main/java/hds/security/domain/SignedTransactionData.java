package hds.security.domain;

import java.util.Base64;

public class SignedTransactionData {
    private final TransactionData payload;
    private final byte[] buyerSignature;
    private byte[] sellerSignature;

    public SignedTransactionData(String buyerSignature, TransactionData data) {
        this.buyerSignature = Base64.getDecoder().decode(buyerSignature);
        this.payload = data;
    }

    public byte[] getBuyerSignature() {
        return buyerSignature;
    }

    public TransactionData getPayload() {
        return payload;
    }

    public void setSellerSignature(byte[] sellerSignature) {
        this.sellerSignature = sellerSignature;
    }

    public byte[] getSellerSignature() {
        return sellerSignature;
    }
}

