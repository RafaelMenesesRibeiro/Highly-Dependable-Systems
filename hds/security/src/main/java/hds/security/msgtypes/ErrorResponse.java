package hds.security.msgtypes;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ErrorResponse extends BasicMessage implements Serializable {
    @NotNull(message = "The error message cannot be null.")
    private String message;

    @NotNull(message = "The error reason cannot be null.")
    private String reason;

    public ErrorResponse(long timestamp, String requestID, String operation, String from, String to, String signature,
                         String message, String reason) {
        super(timestamp, requestID, operation, from, to, signature);
        this.message = message;
        this.reason = reason;
    }

    public ErrorResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", reason='" + reason + '\'' +
                ", requestID=" + requestID +
                ", operation='" + operation + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
