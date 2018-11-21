package ca.inuktitutcomputing.fonts;

import ca.inuktitutcomputing.unitTests.fonts.FontAinunavikTest;
import ca.inuktitutcomputing.unitTests.fonts.FontAipainunaTest;
import ca.inuktitutcomputing.unitTests.fonts.FontAipainunavikTest;
import ca.inuktitutcomputing.unitTests.fonts.FontAujaq2Test;
import ca.inuktitutcomputing.unitTests.fonts.FontAujaqsylTest;
import ca.inuktitutcomputing.unitTests.fonts.FontNaamajutTest;
import ca.inuktitutcomputing.unitTests.fonts.FontNunacomTest;
import ca.inuktitutcomputing.unitTests.fonts.FontOldsylTest;
import ca.inuktitutcomputing.unitTests.fonts.FontProsylTest;
import ca.inuktitutcomputing.unitTests.fonts.FontTest;
import ca.inuktitutcomputing.unitTests.fonts.FontTunngavikTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for fonts");
		//$JUnit-BEGIN$
		suite.addTestSuite(FontAinunavikTest.class);
		suite.addTestSuite(FontAipainunaTest.class);
		suite.addTestSuite(FontAipainunavikTest.class);
		suite.addTestSuite(FontAujaq2Test.class);
		suite.addTestSuite(FontAujaqsylTest.class);
		suite.addTestSuite(FontNaamajutTest.class);
		suite.addTestSuite(FontNunacomTest.class);
		suite.addTestSuite(FontNunacomTest.class);
		suite.addTestSuite(FontOldsylTest.class);
		suite.addTestSuite(FontProsylTest.class);
		suite.addTestSuite(FontTunngavikTest.class);
		suite.addTestSuite(FontTest.class);
		//$JUnit-END$
		return suite;
	}

}
