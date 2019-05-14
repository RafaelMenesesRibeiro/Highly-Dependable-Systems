package hds.server.services;

import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.OwnerDataMessage;
import hds.server.ServerApplication;
import hds.server.controllers.IntentionToSellController;
import org.json.JSONException;
import org.junit.Test;

import java.security.PrivateKey;
import java.sql.SQLException;

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.SecurityManager.setMessageSignature;

public class IntentionToSellControllerTest extends BaseTests {
	public final String OPERATION = "intentionToSell";
	public final String CLIENT_ID = "8001";
	public IntentionToSellController controller;

	@Override
	public void populateForTests() {
		controller = new IntentionToSellController();
	}

	@Test
	public void success() {
		// TODO //
	}

	// TODO - Test sending null fields or empty or not valid fields. //

	@Test(expected = SignatureException.class)
	public void invalidSellerSignature() {
		OwnerDataMessage message = newOwnerDataMessage();
		setMessageSignature(getPrivateKeyFromResource(ServerApplication.getPort()), payload);
		try {
			controller.execute(message);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		}

	}

	private OwnerDataMessage newOwnerDataMessage() {
		return new OwnerDataMessage(
				generateTimestamp(),
				"requestID",
				OPERATION,
				CLIENT_ID,
				"9001",
				"invalid signature",
				"good1",
				CLIENT_ID,
				1,
				true,
				"invalid write operation signature"
		);
	}
}
