package hds.server.services;

import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.OwnerDataMessage;
import hds.server.ServerApplication;
import hds.server.controllers.IntentionToSellController;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.SecurityManager.setMessageSignature;

public class IntentionToSellControllerTest extends BaseTests {
	private final String OPERATION = "intentionToSell";
	private final String CLIENT_ID = "8001";
	private final String NOT_CLIENT_ID = "8002";
	private IntentionToSellController controller;

	@Rule
	public ExpectedException expectedExRule = ExpectedException.none();

	@Override
	public void populateForTests() {
		controller = new IntentionToSellController();
	}

	@Test
	public void success() {
		/*
		OwnerDataMessage message = newOwnerDataMessage();
		try {
			setMessageSignature(getPrivateKeyFromResource(CLIENT_ID), message);
			controller.execute(message);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
		*/
	}

	// TODO - Test sending null fields or empty or not valid fields. //

	@Test
	public void invalidSellerSignature() {
		expectedExRule.expect(RuntimeException.class);
		expectedExRule.expectMessage("The Seller's signature is not valid.");

		OwnerDataMessage message = newOwnerDataMessage();
		try {
			setMessageSignature(getPrivateKeyFromResource(NOT_CLIENT_ID), message);
			controller.execute(message);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void invalidWriteOnGoodsSignature() {
		expectedExRule.expect(RuntimeException.class);
		expectedExRule.expectMessage("The Write On Goods Operation's signature is not valid.");

		OwnerDataMessage message = newOwnerDataMessage();
		try {
			setMessageSignature(getPrivateKeyFromResource(CLIENT_ID), message);
			controller.execute(message);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
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
				"",
				"good1",
				CLIENT_ID,
				1,
				true,
				""
		);
	}
}
