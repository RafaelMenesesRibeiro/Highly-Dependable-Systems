package hds.client.domain;

public class GoodState {
    private int code;
    private String operation;
    private String message;
    private String ownerId;
    private boolean onSale;

    public GoodState() {}

    public GoodState(int code, String operation, String message, String ownerId, boolean onSale) {
        this.code = code;
        this.operation = operation;
        this.message = message;
        this.ownerId = ownerId;
        this.onSale = onSale;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean getOnSale() {
        return onSale;
    }

    public void setOnSale(String onSale) {
        this.onSale = onSale;
    }
}
