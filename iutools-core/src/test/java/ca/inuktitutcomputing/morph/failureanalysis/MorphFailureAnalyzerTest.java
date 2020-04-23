package ca.inuktitutcomputing.morph.failureanalysis;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;

public class MorphFailureAnalyzerTest {

	////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////
	
	@Test
	public void test__MorpFailureAnalyzer__Synopsis() throws Exception {
		// Use this class to identify ngrams that seem to cause words to not be 
		// analyzed by the morphological analyzer.
		//
		//
		// First, you need to get a CompiledCorpus and feed it to an analyzer
		//
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus();
		MorphFailureAnalyzer analyzer = new MorphFailureAnalyzer(corpus);
	}

}
