package ca.inuktitutcomputing.morph.expAlain;


import org.junit.Test;

import ca.inuktitutcomputing.morph.Decomposition;
import ca.nrc.datastructure.Pair;

public class MorphologicalAnalyzer_expAlainTest {

	////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////
	
	@Test
	public void test__decompose__HappyPath() throws Exception {
		MorphologicalAnalyzer_expAlain analyzer = 
				new MorphologicalAnalyzer_expAlain();
		
		String word = "inuktitut";
		DecompositionState state = analyzer.decompose(word);
		DecompositionStateAsserter.assertThat(state, "")
			.containsDecomposition(
				new WrittenMorpheme[] {
					new WrittenMorpheme("inuk/1n", "inuk"),
					new WrittenMorpheme("titut/nn", "titut")
				}
			);
		;
	}

}
