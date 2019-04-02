package com.hds.server.msgtypes;

public class StateOfGood extends BasicResponse{
	private final String ownerId;
	private final boolean onSale;

	public StateOfGood(int code, String message, String operation, String ownerId, boolean onSale) {
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
}
