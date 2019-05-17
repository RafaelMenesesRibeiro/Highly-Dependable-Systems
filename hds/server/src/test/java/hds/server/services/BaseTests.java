package hds.server.services;

import org.junit.After;
import org.junit.Before;

public abstract class BaseTests {
	@Before
	public void setUp() {
		populateForTests();
	}

	@After
	public void tearDown() {
		// Not necessary yet.
	}

	public abstract void populateForTests();
}
