package ca.inuktitutcomputing.unitTests.applications;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for data");
		//$JUnit-BEGIN$
		suite.addTestSuite(DecomposeHansardTestTest.class);

		//$JUnit-END$
		return suite;
	}

}
