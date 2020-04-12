package ca.inuktitutcomputing.morph.expAlain;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class MorphologicalAnalyzer_expAlainTest {

	////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////
	
	@Test @Ignore
	public void test__decomposeWord__HappyPath() throws Exception {
		MorphologicalAnalyzer_expAlain analyzer = 
				new MorphologicalAnalyzer_expAlain();
		
		String word = "inuktitut";
		analyzer.decomposeWord(word);
	}

}
