package hds.server.controllers.controllerHelpers;

import java.util.Objects;

/**
 * Represents a unique identifier for each request, base on the sender and the requestID.
 *
 * @author 		Francisco Barros
 */
public class UserRequestIDKey {
    private final String caller;
    private final String requestID;

    public UserRequestIDKey(String caller, String requestID) {
        this.caller = caller;
        this.requestID = requestID;
    }

    public String getCaller() {
        return caller;
    }

    public String getRequestID() {
        return requestID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRequestIDKey that = (UserRequestIDKey) o;
        return Objects.equals(caller, that.caller) && Objects.equals(requestID, that.requestID);
    }

    @Override
    public int hashCode() {
        return (Objects.hashCode(requestID) << 16) + Objects.hashCode(caller) ;
    }
}
