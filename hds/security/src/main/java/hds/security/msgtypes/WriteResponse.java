package hds.security.msgtypes;

import hds.security.helpers.inputValidation.NotFutureTimestamp;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class WriteResponse extends BasicMessage implements Serializable {
	@NotNull(message = "The write timestamp cannot be null.")
	@NotFutureTimestamp
	private long wts;

	public WriteResponse(long timestamp, String requestID, String operation, String from, String to, String signature, long wts) {
		super(timestamp, requestID, operation, from, to, signature);
		this.wts = wts;
	}

	public WriteResponse() {}

	public long getWts() {
		return wts;
	}

	public void setWts(long wts) {
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
