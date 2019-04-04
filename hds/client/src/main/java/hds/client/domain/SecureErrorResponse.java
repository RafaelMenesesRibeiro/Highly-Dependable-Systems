package hds.client.domain;

import hds.security.msgtypes.response.ErrorResponse;

public class SecureErrorResponse extends SecureResponse {
    private ErrorResponse payload;

    public SecureErrorResponse() {
        super();
    }

    @Override
    public byte[] getSignature() {
        return super.getSignature();
    }

    @Override
    public void setSignature(byte[] signature) {
        super.setSignature(signature);
    }

    public ErrorResponse getPayload() {
        return payload;
    }

    public void setPayload(ErrorResponse payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "SecureErrorResponse{" +
                "payload=" + payload +
                '}';
    }
}
