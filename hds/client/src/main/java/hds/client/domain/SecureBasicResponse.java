package hds.client.domain;

import hds.security.msgtypes.responses.BasicResponse;

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
    public String getSignature() {
        return super.getSignature();
    }

    @Override
    public void setSignature(String signature) {
        super.setSignature(signature);
    }
}
