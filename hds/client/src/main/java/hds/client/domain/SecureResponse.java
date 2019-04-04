package hds.client.domain;

import hds.client.domain.Interfaces.IPayload;
import hds.security.msgtypes.responses.BasicResponse;

public class SecureResponse implements IPayload {
    private String signature;

    SecureResponse() {

    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public BasicResponse getPayload() {
        return new BasicResponse();
    }

    public hds.security.msgtypes.responses.SecureResponse translateSecureResponse() {
        hds.security.msgtypes.responses.SecureResponse secureResponse = new hds.security.msgtypes.responses.SecureResponse();
        secureResponse.setPayload(getPayload());
        secureResponse.setSignature(this.signature);
        return secureResponse;
    }

    public String toString() {
        return signature;
    }
}
