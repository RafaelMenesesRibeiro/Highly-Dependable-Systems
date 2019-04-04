package hds.client.domain;

import hds.security.msgtypes.response.BasicResponse;

public class SecureBasicResponse extends SecureResponse{
    private BasicResponse payload;

    public SecureBasicResponse() {
        super();
    }

    public BasicResponse getPayload() {
        return payload;
    }

    public void setPayload(BasicResponse payload) {
        this.payload = payload;
    }

    @Override
    public void setSignature(byte[] signature) {
        super.setSignature(signature);
    }

    @Override
    public byte[] getSignature() {
        return super.getSignature();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
