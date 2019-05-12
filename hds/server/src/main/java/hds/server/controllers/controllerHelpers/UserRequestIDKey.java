package hds.server.controllers.controllerHelpers;

import java.util.Objects;

/**
 * Represents a unique identifier for each request, base on the sender and the requestID.
 *
 * @author 		Francisco Barros
 */
public class UserRequestIDKey {
    private final String callee;
    private final String requestID;

    public UserRequestIDKey(String callee, String requestID) {
        this.callee = callee;
        this.requestID = requestID;
    }

    public String getCallee() {
        return callee;
    }

    public String getRequestID() {
        return requestID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRequestIDKey that = (UserRequestIDKey) o;
        return Objects.equals(callee, that.callee) && Objects.equals(requestID, that.requestID);
    }

    @Override
    public int hashCode() {
        return (Objects.hashCode(requestID) << 16) + Objects.hashCode(callee) ;
    }
}
