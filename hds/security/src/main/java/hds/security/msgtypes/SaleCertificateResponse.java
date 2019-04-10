package hds.security.msgtypes;

public class SaleCertificateResponse extends BasicMessage {
    private String notaryServer;
    private String goodId;
    private String previousOwner;
    private String newOwner;

    public SaleCertificateResponse(int requestID, String operation, String from, String to, String signature, String notaryServer, String goodId, String previousOwner, String newOwner) {
        super(requestID, operation, from, to, signature);
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
