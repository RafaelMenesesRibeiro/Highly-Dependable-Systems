package hds.server.services;

import org.junit.After;
import org.junit.Before;

import java.security.PrivateKey;
import java.security.PublicKey;

import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.ResourceManager.getPublicKeyFromResource;
import static org.junit.Assert.fail;

public abstract class BaseTests {
	@Before
	public void setUp() {
		// TODO - Create databases. //
		populateForTests();
	}

	@After
	public void tearDown() {
		// Not necessary yet.
	}

	public abstract void populateForTests();
}
