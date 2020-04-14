package ca.inuktitutcomputing.morph.expAlain;

import static org.junit.Assert.*;

import org.junit.Test;

public class MorphoPhonoRulesTest {

	////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////
	
	@Test
	public void test__MorphoPhonoRules__Synopsis() {
		//
		// This class defines the morpho-phonolical rules that transform the 
		// characters at the edges of two consecutive morphemes.
		//
		// You can use it to determine if it's possible for two WrittenMorphemes
		// to be consecutive.
		// 
		WrittenMorpheme morph1 = new WrittenMorpheme("inuk/1n", "inuk");
		WrittenMorpheme morph2 = new WrittenMorpheme("titut/nn", "titut");
		boolean canJoinTogether = 
			MorphoPhonoRules.getInstance().canJoin(morph1, morph2);
	}

}
