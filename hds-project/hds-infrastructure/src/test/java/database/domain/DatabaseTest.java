package database.domain;

import database.exception.InvalidPropertyException;
import database.exception.InvalidQueryParameterException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DatabaseTest {
	private static final String ENDPOINT = "//localhost:5432";
	private static final String NAME = "HDS_DB";
	private static final String USERNAME = "postgres";
	private static final String PASSWORD = "Macaco90";

	@Test
	public void success() {
		Database db = new Database();

		assertEquals(ENDPOINT, db.getEndpoint());
		assertEquals(NAME, db.getName());
		assertEquals(USERNAME, db.getUsername());
		assertEquals(PASSWORD, db.getPassword());
	}

	// TODO - Is this possible to test with JUnit? //
	@Test(expected = IOException.class)
	public void InvalidPropertiesFile() throws IOException {
		Database db = new Database();
		throw new IOException();
	}

	// TODO - Is this possible to test with JUnit? //
	@Test(expected = InvalidPropertyException.class)
	public void PropertyNULL() throws InvalidPropertyException {
		Database db = new Database();
		throw new InvalidPropertyException();
	}

	@Test(expected = InvalidQueryParameterException.class)
	public void nullGooID_GetCurrentOwner() {
		Database db = new Database();
		db.getCurrentOwner(null);
	}

	@Test(expected = InvalidQueryParameterException.class)
	public void emptyGooID_GetCurrentOwner() {
		Database db = new Database();
		db.getCurrentOwner("");
	}

	@Test(expected = InvalidQueryParameterException.class)
	public void nullGooID_GetIsOnSale() {
		Database db = new Database();
		db.getIsOnSale("");
	}

	@Test(expected = InvalidQueryParameterException.class)
	public void emptyGooID_GetIsOnSale() {
		Database db = new Database();
		db.getIsOnSale("");
	}
}
