package org.iutools.morph.l2rAlain;

import org.junit.Test;

public class MorphoPhonoRulesTest {

	////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////
	
	@Test
	public void test__MorphoPhonoRules__Synopsis() throws Exception {
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
