package org.iutools.morph.failureanalysis;

import java.util.List;

import org.junit.Test;

import ca.nrc.datastructure.Pair;

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
				analyzer.getProblems(ProblematicNGram.SortBy.FS_RATIO_THEN_FAILURES);
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
				
				//
				// Total number of OCCURENCES of failing words that contain 
				// the ngram.
				//
				long failureMass = problem.failureMass;
				
//				// Most frequent failing words that contain the ngram
//				// You can get just the words, or the words with their
//				// frequency
//				List<String> failureExamples = problem.failureExamples;
//				List<Pair<String,Long>> failureExamplesWithFreqs =
//						problem.failureExamplesWithFreq();
//
//				// Most frequent succeding words that contain the ngram
//				// You can get just the words, or the words with their
//				// frequency
//				List<String> successExamples = problem.successExamples;
//				List<Pair<String,Long>> successExamplesWithFreqs =
//						problem.successExamplesWithFreq();
			}
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
			.mostProblematicNgramEqual(ProblematicNGram.SortBy.FS_RATIO_THEN_FAILURES,
					new String[] {"inn", "iga", "ssi"})
			
			// Sort the problems by Num failures
			// Note: Order is same as SortBy.FS_Ratio, because 
			//   the latter sorts by FS_RATIO first, and then by
			//   N_FAILURES. And in this particular case, it turns out
			//   to yield the same order
			//
			.mostProblematicNgramEqual(ProblematicNGram.SortBy.N_FAILURES,
					new String[] {"inn", "iga", "ssi"})

			// This is an ngram that ONLY appears in failing 
			// words
			//
			.statsForNgramEqual("iga", 
				Double.MAX_VALUE, 1, null,
				new String[] {"maligaliuqtiuqatitinni"}, 
				new String[] {})
			
			// This is an ngram that appears in some failing and some 
			// succeeding words
			//
			.statsForNgramEqual("liu", 
					0.83, 1, null, 
					new String[] {"maligaliuqtiuqatitinni"}, 
					new String[] {"kiinaujaliurutiqarasuarnirmit", 
						"kiinaujaliurasuanngittut"})
			
			// This is an ngram that has the most number of failures
			.statsForNgramEqual("inn", 
					Double.MAX_VALUE, 2, null, 
					new String[] {"maligaliuqtiuqatitinni", 
						"apiqkusirijassinnut"}, 
					new String[] {})
			
			;
	}

	@Test
	public void test__MorpFailureAnalyzer__WithExclusionPattern() throws Exception {
		MorphFailureAnalyzer analyzer = 
			new MorphFailureAnalyzer()
				.setMinNgramLen(3)
				.setMaxNgramLen(3)
				
				// This will ignore one of the failing words 
				// (apiqkusirijassinnut) which decreases the FS ratio of the 
				// following ngrams:
				//
				//   - inn
				//   - ssi
				//
				// which otherwise would be the first and third topmost 
				// ngrams.
				// 
				.setExclude("(apiq)");
		
		addSomeTestWords(analyzer);
		analyzer.analyseFailures();
		
		new MorphFailureAnalyserAsserter(analyzer, "")
		
			// Sort the problems by Fail/Success ratio
			.mostProblematicNgramEqual(ProblematicNGram.SortBy.FS_RATIO_THEN_FAILURES,
					new String[] {"juk", "iga", "aku"})
			
			.statsForNgramEqual("juk", 
					Double.MAX_VALUE, 1, null,
					new String[] {"takuksaulaarmijuk"}, 
					new String[] {})
			.statsForNgramEqual("iga", 
					Double.MAX_VALUE, 1, null,
					new String[] {"maligaliuqtiuqatitinni"}, 
					new String[] {})
			.statsForNgramEqual("aku", 
					Double.MAX_VALUE, 1, null,
				new String[] {"takuksaulaarmijuk"}, 
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
	}
	
	@Test
	public void test__addWord__NoWordFreq() throws Exception {
		MorphFailureAnalyzer analyzer = 
			new MorphFailureAnalyzer()
				.setMinNgramLen(3)
				.setMaxNgramLen(3);
		
		analyzer.addWord("inuktut", true);
		analyzer.addWord("inuksuk", true);
		analyzer.addWord("inukkkkk", false);
		new MorphFailureAnalyserAsserter(analyzer, "")
		
			.statsForNgramEqual("inu", 
				null, 1, new Long(-1),
				new String[] {"inukkkkk"}, 
				new String[] {"inuksuk", "inuktut"})
			;
	}
	
	@Test
	public void test__addWord__WithWordFreq() throws Exception {
		MorphFailureAnalyzer analyzer = 
			new MorphFailureAnalyzer()
				.setMinNgramLen(3)
				.setMaxNgramLen(3);
		
		analyzer.addWord("inuktut", true, new Long(1000));
		analyzer.addWord("inuksuk", true, new Long(103));
		analyzer.addWord("inukkkkk", false, new Long(18));
		new MorphFailureAnalyserAsserter(analyzer, "")
		
			.statsForNgramEqual("inu", 
				null, 1, new Long(18),
				new String[] {"inukkkkk"}, 
				new String[] {"inuksuk", "inuktut"})
			;
	}
}
