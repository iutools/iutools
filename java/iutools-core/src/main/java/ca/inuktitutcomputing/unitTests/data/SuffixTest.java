package ca.inuktitutcomputing.unitTests.data;

import junit.framework.TestCase;

import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.data.Suffix;
import ca.inuktitutcomputing.data.LinguisticDataAbstract;

public class SuffixTest extends TestCase {
	
	public SuffixTest(String typeOfData) {
		LinguisticDataAbstract.init(typeOfData);
	}
	
	public void testSuffix() {
		Morpheme m = LinguisticDataAbstract.getMorpheme("si/1vv");
		assertTrue("[1] No morpheme found for si/1vv", m != null);
		assertTrue("[1] Wrong morpheme", ((Suffix)m).morpheme.equals("si"));
	}

}