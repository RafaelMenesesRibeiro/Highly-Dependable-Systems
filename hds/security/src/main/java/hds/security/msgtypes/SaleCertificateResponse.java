package hds.security.msgtypes;

import hds.security.helpers.inputValidation.ValidClientID;
import hds.security.helpers.inputValidation.ValidGoodID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class SaleCertificateResponse extends BasicMessage {
    @NotNull(message = "The NotaryCertificationMessage cannot be null.")
    @NotEmpty(message = "The NotaryCertificationMessage cannot be empty.")
    private String notaryServer;

    @NotNull(message = "The GoodID cannot be null.")
    @NotEmpty(message = "The GoodID cannot be empty.")
    @ValidGoodID
    private String goodId;

    @NotNull(message = "The PreviousOwnerID cannot be null.")
    @NotEmpty(message = "The PreviousOwnerID cannot be empty.")
    @ValidClientID
    private String previousOwner;

    @NotNull(message = "The NewOwnerID cannot be null.")
    @NotEmpty(message = "The NewOwnerID cannot be empty.")
    @ValidClientID
    private String newOwner;

    public SaleCertificateResponse(long timestamp, String requestID, String operation, String from, String to,
                                   String signature, String notaryServer, String goodId, String previousOwner, String newOwner) {
        super(timestamp, requestID, operation, from, to, signature);
        this.notaryServer = notaryServer;
        this.goodId = goodId;
        this.previousOwner = previousOwner;
        this.newOwner = newOwner;
    }

    public SaleCertificateResponse() {
    }

    public String getNotaryServer() {
        return notaryServer;
    }

    public void setNotaryServer() {
        this.notaryServer = "Notary Emitted Certificate";
    }

    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }

    public String getPreviousOwner() {
        return previousOwner;
    }

    public void setPreviousOwner(String previousOwner) {
        this.previousOwner = previousOwner;
    }

    public String getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(String newOwner) {
        this.newOwner = newOwner;
    }

    @Override
    public String toString() {
        return "SaleCertificateResponse{" +
                "notaryServer='" + notaryServer + '\'' +
                ", goodId='" + goodId + '\'' +
                ", previousOwner='" + previousOwner + '\'' +
                ", newOwner='" + newOwner + '\'' +
                ", requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
