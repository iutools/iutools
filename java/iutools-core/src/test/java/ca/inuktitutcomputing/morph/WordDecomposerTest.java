package ca.inuktitutcomputing.morph;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;

public class WordDecomposerTest {
	
	@Test
	public void test__decomposeWord__successful_word() throws Exception  {
		WordDecomposer decomposer = new WordDecomposer();
		String word = "nunait";
		try {
			Decomposition[] decs = decomposer.decomposeWord(word);
			assertEquals("The number of decompositions for 'nunait' should be 3.",3, decs.length);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test(expected=Exception.class)
	public void test__decomposeWord__timeout() throws Exception  {
		LinguisticDataSingleton.getInstance("csv");
		WordDecomposer decomposer = new WordDecomposer();
		decomposer.millisTimeout = 3000;
		String word = "ilisaqsitittijunnaqsisimannginnama";
		try {
		decomposer.decomposeWord(word);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

}
