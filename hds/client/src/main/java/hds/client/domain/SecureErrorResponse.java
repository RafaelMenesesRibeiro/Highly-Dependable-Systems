package hds.client.domain;

import hds.security.msgtypes.responses.ErrorResponse;

public class SecureErrorResponse extends SecureResponse {
    private ErrorResponse payload;

    public SecureErrorResponse() {
        super();
    }

    public ErrorResponse getPayload() {
        return payload;
    }

    public void setPayload(ErrorResponse payload) {
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

    @Override
    public String toString() {
        return "SecureErrorResponse{" +
                "payload=" + payload +
                '}';
    }
}
