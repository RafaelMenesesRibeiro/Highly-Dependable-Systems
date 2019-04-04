package hds.security.msgtypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GoodState extends BasicResponse {
	private String ownerId;
	private boolean onSale;

	@JsonCreator
	public GoodState(@JsonProperty("message") String message,
					 @JsonProperty("operation") String operation,
					 String ownerId,
					 boolean onSale) {

		super(message, operation);
		this.ownerId = ownerId;
		this.onSale = onSale;
	}

	public GoodState() {
		// This is here so the class can't be instantiated. //
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public boolean isOnSale() {
		return onSale;
	}

	public void setOnSale(boolean onSale) {
		this.onSale = onSale;
	}

	@Override
	public String toString() {
		return "GoodState{" +
				"ownerId='" + ownerId + '\'' +
				", onSale=" + onSale +
				'}';
	}
}
