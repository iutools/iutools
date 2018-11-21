package ca.inuktitutcomputing.unitTests.fonts;

import ca.inuktitutcomputing.fonts.FontAinunavikTest;
import ca.inuktitutcomputing.fonts.FontAipainunaTest;
import ca.inuktitutcomputing.fonts.FontAipainunavikTest;
import ca.inuktitutcomputing.fonts.FontAujaq2Test;
import ca.inuktitutcomputing.fonts.FontAujaqsylTest;
import ca.inuktitutcomputing.fonts.FontNaamajutTest;
import ca.inuktitutcomputing.fonts.FontNunacomTest;
import ca.inuktitutcomputing.fonts.FontOldsylTest;
import ca.inuktitutcomputing.fonts.FontProsylTest;
import ca.inuktitutcomputing.fonts.FontTest;
import ca.inuktitutcomputing.fonts.FontTunngavikTest;
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
