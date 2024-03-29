package hds.client.services;

import hds.client.ClientApplication;
import hds.client.domain.CallableManager;
import hds.client.helpers.ClientProperties;
import hds.client.helpers.ClientSecurityManager;
import hds.security.ConvertUtils;
import hds.security.CryptoUtils;
import hds.security.DateUtils;
import hds.security.SecurityManager;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.security.msgtypes.GoodStateResponse;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.fail;

@RunWith(JMockit.class)
public class GetStateOfGoodCallableMethodTest extends BaseTests {

	private static int thresholdMajority = 3;

	@Test
	public void getStateOfGoodSuccess(@Mocked final CallableManager callableMgrMock, @Mocked final ClientProperties clientProperties) {
		new Expectations() {{
			ClientProperties.getMyPrivateKey(); returns(
					c1PrivateKey, c1PrivateKey,
					c1PrivateKey, c1PrivateKey,
					c1PrivateKey, c1PrivateKey,
					c1PrivateKey, c1PrivateKey);
		}};

		GoodStateResponse res1 = newMockedGoodStateResponse(C_1_PORT, S_1_PORT, s1PrivateKey);
		GoodStateResponse res2 = newMockedGoodStateResponse(C_1_PORT, S_2_PORT, s2PrivateKey);
		GoodStateResponse res3 = newMockedGoodStateResponse(C_1_PORT, S_3_PORT, s3PrivateKey);
		GoodStateResponse res4 = newMockedGoodStateResponse(C_1_PORT, S_4_PORT, s4PrivateKey);

		new Expectations() {{
			callableMgrMock.call(); returns(res1, res2, res3, res4);
			ClientProperties.getMajorityThreshold(); returns (thresholdMajority);
			ClientProperties.print(anyString); returns(null, null, null);
		}};

		ExecutorService executorService = Executors.newFixedThreadPool(4);
		ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);

		completionService.submit(callableMgrMock);
		completionService.submit(callableMgrMock);
		completionService.submit(callableMgrMock);
		completionService.submit(callableMgrMock);

		if (ClientApplication.processGetStateOfGoodResponses(RID_1,4, completionService) == null) {
			fail("getStateOfGoodSuccess test failed");
		}

		executorService.shutdown();
	}

	@Test
	public void getStateOfGoodError(@Mocked final CallableManager callableMgrMock, @Mocked final ClientProperties clientProperties) {
		new Expectations() {{
			ClientProperties.getMyPrivateKey(); returns(
					c1PrivateKey, c1PrivateKey,
					c1PrivateKey, c1PrivateKey,
					c1PrivateKey, c1PrivateKey);
		}};

		GoodStateResponse res1 = newMockedGoodStateResponse(C_1_PORT, S_1_PORT, s1PrivateKey);
		GoodStateResponse res2 = newMockedGoodStateResponse(C_1_PORT, S_2_PORT, s2PrivateKey);
		GoodStateResponse res3 = newMockedGoodStateResponse(C_1_PORT, S_3_PORT, s3PrivateKey);
		ErrorResponse res4 = newMockedErrorResponse(C_1_PORT, S_4_PORT, s4PrivateKey);

		new Expectations() {{
			callableMgrMock.call(); returns(res1, res2, res3, res4);
			ClientProperties.printError(anyString); returns(null);
			ClientProperties.getMajorityThreshold(); returns (thresholdMajority);
			ClientProperties.print(anyString); returns(null, null, null);
		}};

		ExecutorService executorService = Executors.newFixedThreadPool(4);
		ExecutorCompletionService<BasicMessage> completionService = new ExecutorCompletionService<>(executorService);

		completionService.submit(callableMgrMock);
		completionService.submit(callableMgrMock);
		completionService.submit(callableMgrMock);
		completionService.submit(callableMgrMock);

		if (ClientApplication.processGetStateOfGoodResponses(RID_1,4, completionService) != null) {
			fail("getStateOfGoodSuccess test failed");
		}

		executorService.shutdown();
	}


	/** Helpers */

	private GoodStateResponse newMockedGoodStateResponse(String clientPort, String replicaPort, PrivateKey replicaPrivateKey) {
		try {
			GoodStateResponse response = new GoodStateResponse(
					DateUtils.generateTimestamp(),
					CryptoUtils.newUUIDString(),
					GET_STATE_OF_GOOD,
					replicaPort,
					clientPort,
					"",
					GOOD_1,
					clientPort,
					false,
					clientPort,
					WTS_1,
					ConvertUtils.bytesToBase64String(
							ClientSecurityManager.newWriteOnGoodsDataSignature(GOOD_1, Boolean.FALSE, clientPort, WTS_1)),
					WTS_2,
					ConvertUtils.bytesToBase64String(
							ClientSecurityManager.newWriteOnOwnershipsDataSignature(GOOD_1, clientPort, WTS_2)),
					RID_1);
			SecurityManager.setMessageSignature(replicaPrivateKey, response);
			return response;
		} catch (SignatureException | JSONException exc) {
			fail("Could not generate mocked GoodStateResponse... " + exc.getMessage());
			return null;
		}
	}

	private ErrorResponse newMockedErrorResponse(String clientPort, String replicaPort, PrivateKey replicaPrivateKey) {
		try {
			ErrorResponse response = new ErrorResponse(
					DateUtils.generateTimestamp(),
					CryptoUtils.newUUIDString(),
					"mock",
					replicaPort,
					clientPort,
					"",
					"a_mock_message",
					"a_mock_reason"
			);
			SecurityManager.setMessageSignature(replicaPrivateKey, response);
			return response;
		} catch (SignatureException se) {
			fail("Could not generate mocked ErrorResponse... " + se.getMessage());
			return null;
		}
	}

	private BasicMessage newNullResponse() {
		return null;
	}
}
