package hds.security.domain;

public class GoodData {
	private String goodID;

	public GoodData() {  }

	public GoodData(String goodID) {
		this.goodID = goodID;
	}

	public String getGoodID() {
		return this.goodID;
	}

	public void setGoodID(String goodID) {
		this.goodID = goodID;
	}
}
