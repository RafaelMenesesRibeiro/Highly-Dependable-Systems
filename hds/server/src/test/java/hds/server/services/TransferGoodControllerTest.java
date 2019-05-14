package hds.server.services;

import hds.security.CryptoUtils;
import hds.security.exceptions.SignatureException;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.server.controllers.TransferGoodController;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.domain.ChallengeData;
import hds.server.exception.BadTransactionException;
import hds.server.exception.ChallengeFailedException;
import hds.server.exception.IncorrectSignatureException;
import mockit.Expectations;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.SecurityManager.*;

public class TransferGoodControllerTest extends BaseTests {
	private final String REQUEST_ID = "requestID";
	private final String OPERATION = "transferGood";
	private final String SERVER_ID = "9001";
	private final String BUYER_ID = "8001";
	private final String SELLER_ID = "8002";
	private final String STRANGER_CLIENT_ID = "8003";
	private final String SELLER_OWNED_GOOD_ID = "good2";
	private final String NOT_SELLER_OWNED_GOOD_ID = "good3";
	private final boolean ON_SALE = true;
	private final int WRITE_TIMESTAMP = 1;
	private final String CHALLENGE_ANSWER = "original";
	private TransferGoodController controller;
	private ApproveSaleRequestMessage requestMessage;

	@Rule
	public ExpectedException expectedExRule = ExpectedException.none();

	@Override
	public void populateForTests() {
		controller = new TransferGoodController();
		requestMessage = newApproveSaleRequestMessage();
	}

	// TODO - Test sending null fields or empty or not valid fields. //

	@Test
	public void success() {
		// TODO //
	}

	@Test
	public void buyerIsSeller() {
		expectedExRule.expect(BadTransactionException.class);
		expectedExRule.expectMessage("BuyerID cannot be equal to SellerID ");

		requestMessage.setBuyerID(SELLER_ID);
		try {
			controller.execute(requestMessage);
		}
		catch (SQLException | JSONException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test(expected = ChallengeFailedException.class)
	public void wrongChallengeAnswer() {
		requestMessage.setChallengeResponse(CHALLENGE_ANSWER + "wrong");
		ChallengeData replicaChallengeData = new ChallengeData(REQUEST_ID, CHALLENGE_ANSWER, "hsahed", new char[5]);

		new Expectations(GeneralControllerHelper.class) {{ GeneralControllerHelper.removeAndReturnChallenge((UserRequestIDKey) any); returns(replicaChallengeData); }};

		try {
			controller.execute(requestMessage);
		}
		catch (SQLException | JSONException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void emptySellerSignature() {
		expectedExRule.expect(SignatureException.class);
		expectedExRule.expectMessage("Signature length not correct:");

		requestMessage.setChallengeResponse(CHALLENGE_ANSWER);
		ChallengeData replicaChallengeData = new ChallengeData(REQUEST_ID, CHALLENGE_ANSWER, "hsahed", new char[5]);

		new Expectations(GeneralControllerHelper.class) {{ GeneralControllerHelper.removeAndReturnChallenge((UserRequestIDKey) any); returns(replicaChallengeData); }};

		try {
			controller.execute(requestMessage);
		}
		catch (SQLException | JSONException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test(expected = IncorrectSignatureException.class)
	public void incorrectSellerSignature() {
		requestMessage.setChallengeResponse(CHALLENGE_ANSWER);
		ChallengeData replicaChallengeData = new ChallengeData(REQUEST_ID, CHALLENGE_ANSWER, "hsahed", new char[5]);

		new Expectations(GeneralControllerHelper.class) {{ GeneralControllerHelper.removeAndReturnChallenge((UserRequestIDKey) any); returns(replicaChallengeData); }};

		try {
			setMessageWrappingSignature(getPrivateKeyFromResource(STRANGER_CLIENT_ID), requestMessage);
			controller.execute(requestMessage);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void emptyWriteOnOwnershipSignature() {
		expectedExRule.expect(SignatureException.class);
		expectedExRule.expectMessage("The Write On Ownership Operation's signature is not valid.");

		requestMessage.setChallengeResponse(CHALLENGE_ANSWER);
		ChallengeData replicaChallengeData = new ChallengeData(REQUEST_ID, CHALLENGE_ANSWER, "hsahed", new char[5]);

		new Expectations(GeneralControllerHelper.class) {{ GeneralControllerHelper.removeAndReturnChallenge((UserRequestIDKey) any); returns(replicaChallengeData); }};

		try {
			setMessageWrappingSignature(getPrivateKeyFromResource(SELLER_ID), requestMessage);
			controller.execute(requestMessage);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void incorrectWriteOnOwnershipSignature() {
		expectedExRule.expect(SignatureException.class);
		expectedExRule.expectMessage("The Write On Ownership Operation's signature is not valid.");

		requestMessage.setChallengeResponse(CHALLENGE_ANSWER);
		ChallengeData replicaChallengeData = new ChallengeData(REQUEST_ID, CHALLENGE_ANSWER, "hsahed", new char[5]);

		new Expectations(GeneralControllerHelper.class) {{ GeneralControllerHelper.removeAndReturnChallenge((UserRequestIDKey) any); returns(replicaChallengeData); }};

		try {
			byte[] rawData = newWriteOnOwnershipData(SELLER_OWNED_GOOD_ID, BUYER_ID, WRITE_TIMESTAMP).toString().getBytes();
			String writeOnOwnershipSignature = bytesToBase64String(CryptoUtils.signData(getPrivateKeyFromResource(STRANGER_CLIENT_ID), rawData));
			requestMessage.setWriteOnOwnershipsSignature(writeOnOwnershipSignature);

			setMessageWrappingSignature(getPrivateKeyFromResource(SELLER_ID), requestMessage);
			controller.execute(requestMessage);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void emptyWriteOnGoodsSignature() {
		expectedExRule.expect(SignatureException.class);
		expectedExRule.expectMessage("The Write On Goods Operation's signature is not valid.");

		requestMessage.setChallengeResponse(CHALLENGE_ANSWER);
		ChallengeData replicaChallengeData = new ChallengeData(REQUEST_ID, CHALLENGE_ANSWER, "hsahed", new char[5]);

		new Expectations(GeneralControllerHelper.class) {{ GeneralControllerHelper.removeAndReturnChallenge((UserRequestIDKey) any); returns(replicaChallengeData); }};

		try {
			byte[] rawData = newWriteOnOwnershipData(SELLER_OWNED_GOOD_ID, BUYER_ID, WRITE_TIMESTAMP).toString().getBytes();
			String writeOnOwnershipSignature = bytesToBase64String(CryptoUtils.signData(getPrivateKeyFromResource(BUYER_ID), rawData));
			requestMessage.setWriteOnOwnershipsSignature(writeOnOwnershipSignature);

			setMessageWrappingSignature(getPrivateKeyFromResource(SELLER_ID), requestMessage);
			controller.execute(requestMessage);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	private ApproveSaleRequestMessage newApproveSaleRequestMessage() {
		return new ApproveSaleRequestMessage(
				generateTimestamp(),
				REQUEST_ID,
				OPERATION,
				BUYER_ID,
				SELLER_ID,
				"",
				SELLER_OWNED_GOOD_ID,
				BUYER_ID,
				SELLER_ID,
				WRITE_TIMESTAMP,
				ON_SALE,
				"",
				"",
				generateTimestamp(),
				OPERATION,
				SELLER_ID,
				SERVER_ID,
				"",
				""
		);
	}

	public class MockedConnection implements Connection {

		@Override
		public Statement createStatement() throws SQLException {
			return null;
		}

		@Override
		public PreparedStatement prepareStatement(String sql) throws SQLException {
			return null;
		}

		@Override
		public CallableStatement prepareCall(String sql) throws SQLException {
			return null;
		}

		@Override
		public String nativeSQL(String sql) throws SQLException {
			return null;
		}

		@Override
		public void setAutoCommit(boolean autoCommit) throws SQLException {

		}

		@Override
		public boolean getAutoCommit() throws SQLException {
			return false;
		}

		@Override
		public void commit() throws SQLException {

		}

		@Override
		public void rollback() throws SQLException {

		}

		@Override
		public void close() throws SQLException {

		}

		@Override
		public boolean isClosed() throws SQLException {
			return false;
		}

		@Override
		public DatabaseMetaData getMetaData() throws SQLException {
			return null;
		}

		@Override
		public void setReadOnly(boolean readOnly) throws SQLException {

		}

		@Override
		public boolean isReadOnly() throws SQLException {
			return false;
		}

		@Override
		public void setCatalog(String catalog) throws SQLException {

		}

		@Override
		public String getCatalog() throws SQLException {
			return null;
		}

		@Override
		public void setTransactionIsolation(int level) throws SQLException {

		}

		@Override
		public int getTransactionIsolation() throws SQLException {
			return 0;
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			return null;
		}

		@Override
		public void clearWarnings() throws SQLException {

		}

		@Override
		public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
			return null;
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return null;
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return null;
		}

		@Override
		public Map<String, Class<?>> getTypeMap() throws SQLException {
			return null;
		}

		@Override
		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

		}

		@Override
		public void setHoldability(int holdability) throws SQLException {

		}

		@Override
		public int getHoldability() throws SQLException {
			return 0;
		}

		@Override
		public Savepoint setSavepoint() throws SQLException {
			return null;
		}

		@Override
		public Savepoint setSavepoint(String name) throws SQLException {
			return null;
		}

		@Override
		public void rollback(Savepoint savepoint) throws SQLException {

		}

		@Override
		public void releaseSavepoint(Savepoint savepoint) throws SQLException {

		}

		@Override
		public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return null;
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return null;
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return null;
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
			return null;
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
			return null;
		}

		@Override
		public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
			return null;
		}

		@Override
		public Clob createClob() throws SQLException {
			return null;
		}

		@Override
		public Blob createBlob() throws SQLException {
			return null;
		}

		@Override
		public NClob createNClob() throws SQLException {
			return null;
		}

		@Override
		public SQLXML createSQLXML() throws SQLException {
			return null;
		}

		@Override
		public boolean isValid(int timeout) throws SQLException {
			return false;
		}

		@Override
		public void setClientInfo(String name, String value) throws SQLClientInfoException {

		}

		@Override
		public void setClientInfo(Properties properties) throws SQLClientInfoException {

		}

		@Override
		public String getClientInfo(String name) throws SQLException {
			return null;
		}

		@Override
		public Properties getClientInfo() throws SQLException {
			return null;
		}

		@Override
		public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
			return null;
		}

		@Override
		public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
			return null;
		}

		@Override
		public void setSchema(String schema) throws SQLException {

		}

		@Override
		public String getSchema() throws SQLException {
			return null;
		}

		@Override
		public void abort(Executor executor) throws SQLException {

		}

		@Override
		public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

		}

		@Override
		public int getNetworkTimeout() throws SQLException {
			return 0;
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return null;
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return false;
		}
	}
}
