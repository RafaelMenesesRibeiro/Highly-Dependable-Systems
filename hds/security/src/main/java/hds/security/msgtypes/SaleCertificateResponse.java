package hds.security.msgtypes;

import hds.security.helpers.inputValidation.NotFutureTimestamp;
import hds.security.helpers.inputValidation.ValidClientID;
import hds.security.helpers.inputValidation.ValidGoodID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class SaleCertificateResponse extends BasicMessage implements Serializable {
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

    @NotNull(message = "The wts cannot be null.")
    private int wts;

    public SaleCertificateResponse(long timestamp,
                                   String requestID,
                                   String operation,
                                   String from,
                                   String to,
                                   String signature,
                                   String notaryServer,
                                   String goodId,
                                   String previousOwner,
                                   String newOwner,
                                   int wts) {

        super(timestamp, requestID, operation, from, to, signature);
        this.notaryServer = notaryServer;
        this.goodId = goodId;
        this.previousOwner = previousOwner;
        this.newOwner = newOwner;
        this.wts = wts;
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

    public void setNotaryServer(String notaryServer) {
        this.notaryServer = notaryServer;
    }

    public int getWts() {
        return wts;
    }

    public void setWts(int wts) {
        this.wts = wts;
    }

    @Override
    public String toString() {
        return "SaleCertificateResponse{" +
                "notaryServer='" + notaryServer + '\'' +
                ", goodId='" + goodId + '\'' +
                ", previousOwner='" + previousOwner + '\'' +
                ", newOwner='" + newOwner + '\'' +
                ", wts=" + wts +
                ", requestID='" + requestID + '\'' +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
