package hds.client.domain;

import java.security.PublicKey;

import static hds.security.SecurityManager.verifySignature;

public class GoodStateSuccessResponse {
    private GoodState goodState;
    private byte[] signature;

    public GoodStateSuccessResponse() {

    }

    public GoodStateSuccessResponse(GoodState goodState, byte[] signature) {
        this.goodState = goodState;
        this.signature = signature;
    }

    public GoodState getGoodState() {
        return goodState;
    }

    public void setGoodState(GoodState goodState) {
        this.goodState = goodState;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public boolean verifySignature(PublicKey publicKey) {
        return verifySignature(publicKey, signature, SecurityManager.getBytes(payload));
    }
}
