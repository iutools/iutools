package org.iutools.morph.expAlain;


import org.junit.Test;

public class MorphologicalAnalyzer_expAlainTest {

	////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////
	
	@Test
	public void test__decompose__HappyPath() throws Exception {
		MorphologicalAnalyzer_L2R analyzer = 
				new MorphologicalAnalyzer_L2R();
		
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
