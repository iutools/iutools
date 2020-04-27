package ca.inuktitutcomputing.morph.failureanalysis;

import java.util.List;

import org.junit.Test;

import ca.inuktitutcomputing.morph.failureanalysis.ProblematicNGram.SortBy;

public class MorphFailureAnalyzerTest {

	////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////
	
	@Test
	public void test__MorpFailureAnalyzer__Synopsis() throws Exception {
		// Use this class to identify ngrams that seem to cause words to not be 
		// analyzed by the morphological analyzer.
		//
		// First, you need to tell the analyzer about words that succeed or 
		// fail.
		//
		MorphFailureAnalyzer analyzer = new MorphFailureAnalyzer();
		
		// These are words that decompose successfully
		analyzer.addWord("nutaaminirmik", true);
		analyzer.addWord("kiinaujaliurutiqarasuarnirmit", true);
		analyzer.addWord("ikajuutitaqarmijuq", true);
		analyzer.addWord("kiinaujaliurasuanngittut", true);
		analyzer.addWord("unaulluni", true);
		
		// These are words that do NOT decompose
		analyzer.addWord("takuksaulaarmijuk", false);
		analyzer.addWord("maligaliuqtiuqatitinni", false);
		analyzer.addWord("apiqkusirijassinnut", false);
		analyzer.addWord("auktajuumik", false);
		analyzer.addWord("qaujivvigittialaurnatigut", false);
		
		// At this point, you ask for an analysis of the failures
		//
		analyzer.analyseFailures();
		
		// You can then get a list of "problematic" ngrams, sorted by 
		// different criteria (see below for definition of each).
		//
		// For example, below we sort by:
		// - the Fail/Success ratio first
		//
		List<ProblematicNGram> problems = 
				analyzer.getProblems(SortBy.FS_RATIO);
		for (ProblematicNGram problem: problems) {
			//
			// Each ngram provides information that can be used to prioritize 
			// problems to be fixed.
			//
			{
				//
				// (# failing words that contain the ngram) /
				//    (# succeeding words that contain the ngram)
				//
				// ratio > 1 means that this ngram is more frequent in failing 
				// words than in successful ones. Therefore there is a good 
				// chance that this ngram corresponds to:
				//
				// - a morphme that is missing from the DB
				// - a missing written form of an existing morpheme
				// - a missing morpho-phonetic rule 
				// 
				double failSuccedRatio = problem.getFailSucceedRatio();
				
				// 
				// (# failing words that contain the ngram)
				//
				// A high value means that solving this problem will solve a 
				// lot of curently failing words
				//
				long numFailure = problem.getNumFailures();
			}
			
			// You can also get a sample of successful/failing words
			// that contain that ngram. This should allow you to zero-in on the
			// nature of the problem that the morphological analyzer has with 
			// this ngram.
			//
			int sampleSize = 20;
			List<String> failingWordsSample = 
				analyzer.failureExamplesFor(problem);
			List<String> succeedingWordsSample = 
					analyzer.successExamplesFor(problem);
		}
	}

	//////////////////////////
	// VERIFICATION TESTS
	//////////////////////////

	@Test
	public void test__MorpFailureAnalyzer__HappyPath() throws Exception {
		MorphFailureAnalyzer analyzer = 
			new MorphFailureAnalyzer()
				.setMinNgramLen(3)
				.setMaxNgramLen(3);
		addSomeTestWords(analyzer);
		analyzer.analyseFailures();
		
		new MorphFailureAnalyserAsserter(analyzer, "")
		
			// Sort the problems by Fail/Success ratio
			.mostProblematicNgramEqual(SortBy.FS_RATIO, 
					new String[] {"ati", "inn", "iga"})
			
			// Sort the problems by Num failures
			// Note: Order is same as SortBy.FS_Ratio, because 
			//   the latter sorts by FS_RATIO first, and then by
			//   N_FAILURES. And in this particular case, it turns out
			//   to yield the same order
			//
			.mostProblematicNgramEqual(SortBy.N_FAILURES, 
					new String[] {"ati", "inn", "iga"})

			// This is an ngram that ONLY appears in failing 
			// words
			//
			.statsForNgramEqual("iga", Double.MAX_VALUE, 1, 
				new String[] {"maligaliuqtiuqatitinni"}, 
				new String[] {})
			
			// This is an ngram that appears in some failing and some 
			// succeeding words
			//
			.statsForNgramEqual("liu", 0.5, 1, 
					new String[] {"maligaliuqtiuqatitinni"}, 
					new String[] {"kiinaujaliurutiqarasuarnirmit", 
						"kiinaujaliurasuanngittut"})
			
			// This is an ngram that has the most number of failures
			.statsForNgramEqual("ati", Double.MAX_VALUE, 2, 
					new String[] {"maligaliuqtiuqatitinni", 
						"qaujivvigittialaurnatigut"}, 
					new String[] {})
			
			;
	}

	//////////////////////////
	// TEST HELPERS
	//////////////////////////
	
	private void addSomeTestWords(MorphFailureAnalyzer analyzer) {
		// These are words that decompose successfully
		analyzer.addWord("nutaaminirmik", true);
		analyzer.addWord("kiinaujaliurutiqarasuarnirmit", true);
		analyzer.addWord("ikajuutitaqarmijuq", true);
		analyzer.addWord("kiinaujaliurasuanngittut", true);
		analyzer.addWord("unaulluni", true);
		
		// These are words that do NOT decompose
		analyzer.addWord("takuksaulaarmijuk", false);
		analyzer.addWord("maligaliuqtiuqatitinni", false);
		analyzer.addWord("apiqkusirijassinnut", false);
		analyzer.addWord("auktajuumik", false);
		analyzer.addWord("qaujivvigittialaurnatigut", false);
	}
}
