package hds.client.domain;

import hds.client.domain.Interfaces.IPayload;
import hds.security.msgtypes.response.BasicResponse;

import java.util.Arrays;

public class SecureResponse implements IPayload {
    private byte[] signature;

    SecureResponse() {

    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "SecureResponse{" +
                "signature=" + Arrays.toString(signature) +
                '}';
    }

    public BasicResponse getPayload() {
        return new BasicResponse();
    }

    public hds.security.msgtypes.response.SecureResponse translateSecureResponse() {
        hds.security.msgtypes.response.SecureResponse secureResponse = new hds.security.msgtypes.response.SecureResponse();
        secureResponse.setPayload(getPayload());
        secureResponse.setSignature(this.signature);
        return secureResponse;
    }
}
