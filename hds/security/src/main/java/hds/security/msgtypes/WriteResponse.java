package hds.security.msgtypes;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class WriteResponse extends BasicMessage implements Serializable {
	@NotNull(message = "The write timestamp cannot be null.")
	private int writeTimestamp;

	public WriteResponse(long timestamp, String requestID, String operation, String from, String to, String signature, int writeTimestamp) {
		super(timestamp, requestID, operation, from, to, signature);
		this.writeTimestamp = writeTimestamp;
	}

	public WriteResponse() {}

	public int getWriteTimestamp() {
		return writeTimestamp;
	}

	public void setWriteTimestamp(int writeTimestamp) {
		this.writeTimestamp = writeTimestamp;
	}

	@Override
	public String toString() {
		return "WriteResponse{" +
				"writeTimestamp=" + writeTimestamp +
				", requestID='" + requestID + '\'' +
				", operation='" + operation + '\'' +
				", from='" + from + '\'' +
				", to='" + to + '\'' +
				", signature='" + signature + '\'' +
				'}';
	}
}
