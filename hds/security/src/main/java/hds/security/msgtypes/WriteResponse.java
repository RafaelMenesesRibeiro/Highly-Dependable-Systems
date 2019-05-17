package hds.security.msgtypes;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class WriteResponse extends BasicMessage implements Serializable {
	@NotNull(message = "The write timestamp cannot be null.")
	private int wts;

	// TODO - Should it be wts that's being sent to verify? //
	public WriteResponse(long timestamp, String requestID, String operation, String from, String to, String signature, int wts) {
		super(timestamp, requestID, operation, from, to, signature);
		this.wts = wts;
	}

	public WriteResponse() {}

	public int getWts() {
		return wts;
	}

	public void setWts(int wts) {
		this.wts = wts;
	}

	@Override
	public String toString() {
		return "WriteResponse{" +
				"wts=" + wts +
				", requestID='" + requestID + '\'' +
				", operation='" + operation + '\'' +
				", from='" + from + '\'' +
				", to='" + to + '\'' +
				", signature='" + signature + '\'' +
				'}';
	}
}
