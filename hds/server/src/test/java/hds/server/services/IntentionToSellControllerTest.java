package hds.server.services;

import hds.security.CryptoUtils;
import hds.security.msgtypes.OwnerDataMessage;
import hds.server.ServerApplication;
import hds.server.controllers.IntentionToSellController;
import hds.server.exception.NoPermissionException;
import hds.server.exception.OldMessageException;
import hds.server.helpers.DatabaseManager;
import hds.server.helpers.MarkForSale;
import hds.server.helpers.TransactionValidityChecker;
import mockit.Expectations;
import mockit.integration.junit4.JMockit;
import org.apache.catalina.Server;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import static hds.security.ConvertUtils.bytesToBase64String;
import static hds.security.DateUtils.generateTimestamp;
import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.SecurityManager.newWriteOnGoodsData;
import static hds.security.SecurityManager.setMessageSignature;

@RunWith(JMockit.class)
public class IntentionToSellControllerTest extends BaseTests {
	private final String OPERATION = "intentionToSell";
	private final String SERVER_ID = "9001";
	private final String CLIENT_ID = "8001";
	private final String NOT_CLIENT_ID = "8002";
	private final String OWNED_GOOD_ID = "good1";
	private final String NOT_OWNED_GOOD_ID = "good2";
	private final boolean ON_SALE = true;
	private final int WRITE_TIMESTAMP = 1;
	private IntentionToSellController controller;
	private OwnerDataMessage ownerDataMessage;

	@Rule
	public ExpectedException expectedExRule = ExpectedException.none();

	@Override
	public void populateForTests() {
		controller = new IntentionToSellController();
		ownerDataMessage = newOwnerDataMessage();
	}

	@Test
	public void success() {
		new Expectations(DatabaseManager.class) {{
			try { DatabaseManager.getConnection(); this.result = new MockedConnection(); }
			catch (SQLException ex) { /* Do nothing. */ }
		}};

		new Expectations(TransactionValidityChecker.class) {{
			try { TransactionValidityChecker.getCurrentOwner((Connection) any, anyString); returns(CLIENT_ID); }
			catch (JSONException | SQLException e) { /* Do nothing. */ }
		}};

		new Expectations(ServerApplication.class) {{ ServerApplication.getMyWts(); returns(1); }};

		new Expectations(MarkForSale.class) {{
			try { MarkForSale.changeGoodSaleStatus((Connection) any, anyString, anyBoolean, anyString, anyString, anyString); }
			catch (JSONException | SQLException ex) { /* Do nothing. */ }
		}};

		ownerDataMessage.setWriteTimestamp(2);
		try {
			PrivateKey privateKey = getPrivateKeyFromResource(CLIENT_ID);

			byte[] rawData = newWriteOnGoodsData(OWNED_GOOD_ID, ON_SALE, CLIENT_ID, ownerDataMessage.getWriteTimestamp()).toString().getBytes();
			String writeOnGoodsSignature = bytesToBase64String(CryptoUtils.signData(privateKey, rawData));
			ownerDataMessage.setWriteOperationSignature(writeOnGoodsSignature);

			setMessageSignature(privateKey, ownerDataMessage);

			controller.execute(ownerDataMessage);
		}
		catch (JSONException | SQLException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	// TODO - Test sending null fields or empty or not valid fields. //

	@Test
	public void invalidSellerSignature() {
		expectedExRule.expect(RuntimeException.class);
		expectedExRule.expectMessage("The Seller's signature is not valid.");

		try {
			setMessageSignature(getPrivateKeyFromResource(NOT_CLIENT_ID), ownerDataMessage);
			controller.execute(ownerDataMessage);
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

		try {
			setMessageSignature(getPrivateKeyFromResource(CLIENT_ID), ownerDataMessage);
			controller.execute(ownerDataMessage);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test(expected = NoPermissionException.class)
	public void clientNotOwnerOfGood() {
		new Expectations(DatabaseManager.class) {{
			try { DatabaseManager.getConnection(); this.result = new MockedConnection(); }
			catch (SQLException ex) { /* Do nothing. */ }
		}};

		new Expectations(TransactionValidityChecker.class) {{
			try { TransactionValidityChecker.getCurrentOwner((Connection) any, anyString); returns("8002"); }
			catch (JSONException | SQLException e) { /* Do nothing. */ }
		}};

		ownerDataMessage.setGoodID(NOT_OWNED_GOOD_ID);
		try {
			PrivateKey privateKey = getPrivateKeyFromResource(CLIENT_ID);

			byte[] rawData = newWriteOnGoodsData(NOT_OWNED_GOOD_ID, ON_SALE, CLIENT_ID, WRITE_TIMESTAMP).toString().getBytes();
			String writeOnGoodsSignature = bytesToBase64String(CryptoUtils.signData(privateKey, rawData));
			ownerDataMessage.setWriteOperationSignature(writeOnGoodsSignature);

			setMessageSignature(privateKey, ownerDataMessage);

			controller.execute(ownerDataMessage);
		}
		catch (SQLException | JSONException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
			// Test failed
			System.out.println(ex.getMessage());
		}
	}

	@Test(expected = OldMessageException.class)
	public void writeTimestampIsTooOld() {
		new Expectations(DatabaseManager.class) {{
			try { DatabaseManager.getConnection(); this.result = new MockedConnection(); }
			catch (SQLException ex) { /* Do nothing. */ }
		}};

		new Expectations(TransactionValidityChecker.class) {{
			try { TransactionValidityChecker.getCurrentOwner((Connection) any, anyString); returns(CLIENT_ID); }
			catch (JSONException | SQLException e) { /* Do nothing. */ }
		}};

		new Expectations(ServerApplication.class) {{ ServerApplication.getMyWts(); returns(3); }};

		ownerDataMessage.setWriteTimestamp(1);
		try {
			PrivateKey privateKey = getPrivateKeyFromResource(CLIENT_ID);

			byte[] rawData = newWriteOnGoodsData(OWNED_GOOD_ID, ON_SALE, CLIENT_ID, ownerDataMessage.getWriteTimestamp()).toString().getBytes();
			String writeOnGoodsSignature = bytesToBase64String(CryptoUtils.signData(privateKey, rawData));
			ownerDataMessage.setWriteOperationSignature(writeOnGoodsSignature);

			setMessageSignature(privateKey, ownerDataMessage);

			controller.execute(ownerDataMessage);
		}
		catch (JSONException | SQLException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | java.security.SignatureException ex) {
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
				SERVER_ID,
				"",
				OWNED_GOOD_ID,
				CLIENT_ID,
				WRITE_TIMESTAMP,
				ON_SALE,
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
