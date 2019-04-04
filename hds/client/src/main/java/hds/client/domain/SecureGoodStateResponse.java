package hds.client.domain;
import hds.security.msgtypes.GoodState;

public class SecureGoodStateResponse extends SecureResponse{
    private GoodState payload;

    public SecureGoodStateResponse() {
        super();
    }

    public GoodState getPayload() {
        return payload;
    }

    public void setPayload(GoodState payload) {
        this.payload = payload;
    }

    @Override
    public byte[] getSignature() {
        return super.getSignature();
    }

    @Override
    public void setSignature(byte[] signature) {
        super.setSignature(signature);
    }
}
