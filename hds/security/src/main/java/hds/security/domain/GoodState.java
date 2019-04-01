package hds.security.domain;

public class GoodState extends BasicResponse{
	private final String ownerId;
	private final boolean onSale;

	public GoodState(int code, String message, String operation, String ownerId, boolean onSale) {
		super(code, message, operation);
		this.ownerId = ownerId;
		this.onSale = onSale;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public boolean getOnSale() {
		return onSale;
	}

	@Override
	public String toString() {
		return "GoodState{" +
				"ownerId='" + ownerId + '\'' +
				", onSale=" + onSale +
				'}';
	}
}
