package ca.pirurvik.iutools.spellchecker;

import static org.junit.Assert.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.CompiledCorpusRegistry;

public class SpellCheckerAccuracyTest {
	
	SpellChecker checkerLargeDict = null;
	
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	
	private SpellChecker getLargeDictChecker() throws StringSegmenterException, SpellCheckerException {
		if (checkerLargeDict == null) {
			checkerLargeDict = new SpellChecker();
		}
		return checkerLargeDict;
	}	
	
	//
	// With a few exceptions, the examples below are a subset of the 200 most 
	// frequent spelling mistakes in the Hansard.
	//
	// This subset excludes:
	// - Proper nouns
	// - Words borrowed from English (ex: minista = minister)
	//
	private static final SpellCheckerExample[] 
			examples_MostFrequenMisspelledWords = new SpellCheckerExample[] {
			
		// NEED-IMPROVEMENT: Examples with ranking > 5
		new SpellCheckerExample("nakuqmi", 6, "nakurmiik"),					
		new SpellCheckerExample("nunavungmi", 12, "nunavummi"),
		new SpellCheckerExample("nunavuumik", 26, "nunavummik"),
		new SpellCheckerExample("nunavuumit", 37, "nunavummit"),
					
		// OK: Examples with ranking <= 5
		new SpellCheckerExample("akitujutinut", 5, "akitujuutinut"),
		new SpellCheckerExample("arragumi", 5, "arraagumi"),
		new SpellCheckerExample("asuillaak", 5, "asuilaak"),
		new SpellCheckerExample("iksivauitaaq", 5, "iksivautaaq", "iksivautaak", "issivautaaq", "issivautaak", "itsivautaaq", "itsivautaak"),
		new SpellCheckerExample("iksivautap", 5, "iksivautaup"),
		new SpellCheckerExample("immaqa", 5, "immaqaa"),
		new SpellCheckerExample("katimajit", 5, "katimajiit"),
		new SpellCheckerExample("katimmajjutiksaq", 5, "katimajjutiksaq"),
		new SpellCheckerExample("kiinaujatigut", 5, "kiinaujaqtigut"),
		new SpellCheckerExample("kiinaujat", 5, "kiinaujait"),		
		new SpellCheckerExample("maligaliqtit", 5, "maligaliqtiit"),
		new SpellCheckerExample("maligatigut", 5, "maligaqtigut"),		
		new SpellCheckerExample("nigiani", 5, "niggiani"),
		new SpellCheckerExample("nniaqamangittulirijiit", 5, "aanniaqamangittulirijiit"),
		new SpellCheckerExample("nniaqamangittulirinirmut", 5, "aanniaqamangittulirinirmut"),
		new SpellCheckerExample("nniaqtulirinirmut", 5, "aanniaqtulirinirmut"),
		new SpellCheckerExample("nunavumi", 5, "nunavummi"),
		new SpellCheckerExample("nunavumiut", 5, "nunavummiut"),
		new SpellCheckerExample("nunavumut", 5, "nunavummut"),
		new SpellCheckerExample("nunavutmi", 5, "nunavummi"),
		new SpellCheckerExample("pigiaqtitat", 5, "pigiaqtitait"),
		new SpellCheckerExample("qallunaatitut", 5, "qallunaaqtitut"),
		new SpellCheckerExample("sulikkanniiq", 5, "sulikkanniq"),
		new SpellCheckerExample("takkua", 5, "taakkua"),
		new SpellCheckerExample("tamakkuninnga", 5, "tamakkuninga"),
		new SpellCheckerExample("tamatuminnga", 5, "tamatuminga"),
		new SpellCheckerExample("tamatumunnga", 5, "tamatumunga"),
		new SpellCheckerExample("tanna", 5, "taanna"),		
		new SpellCheckerExample("tavani", 5, "tavvani"),
		new SpellCheckerExample("ugaalautaa", 5, "uqaalautaa"),
		new SpellCheckerExample("uvalu", 5, "uvvalu"),
		new SpellCheckerExample("tamaini", 5, "tamainni"),
		new SpellCheckerExample("nniaqtulirijikkunnut", 5, "aanniaqtulirijikkunnut"),
		new SpellCheckerExample("immaqaqai", 5, "immaqaaqai"),
		new SpellCheckerExample("taimak", 5, "taimaak")
	};
		
	//
	// These examples were handpicked through a "butterfly collection" type of
	// approach (i.e. try the SpellChecker on different texts and note 
	// "interesting" failing cases).
	//	
	private static final SpellCheckerExample[] 
			examples_HandPickedMispelledWords = new SpellCheckerExample[] {
				// NEEDS-IMPROVEMENT: rank > 5 or null
				new SpellCheckerExample("piliriqatigiinik", null, "piliriqatigiinnik"),
				
				// OK: rank <= 5
				new SpellCheckerExample("aanniaqarnngittulirijikkut", 5, "aanniaqanngittulirijikkut"),
				new SpellCheckerExample("angijuqqaaqaqtutik", 5, "angajuqqaaqaqtutik"),					
				new SpellCheckerExample("maliklugu", 5, "maliglugu"),
				new SpellCheckerExample("pivagiijainiq", 5, "pivagiijarniq"),
				new SpellCheckerExample("qassigasangnut", 5, "qassigalangnut"),
				new SpellCheckerExample("qaujisarutinginniklu", 5, "qaujisarutinginniglu"),
				new SpellCheckerExample("qaritaujarmuaqtiqtaujuni", 5, "qaritaujamuaqtitaujuni"),
				new SpellCheckerExample("silataaniingaaqtulirinirmut", 5, "silataaninngaaqtulirinirmut"),
				new SpellCheckerExample("sivunnganit", 5, "sivuninganit", "sivurnganit"),
				new SpellCheckerExample("tukimuaktittiniaqtumik", 5, "tukimuaqtittiniaqtumik"),
				new SpellCheckerExample("tukimuaktiungmata", 5, "tukimuaqtiungmata"),
				new SpellCheckerExample("upalungaijanirmut", 5, "upalungaijarnirmut"),
				new SpellCheckerExample("uqaujjigiarutiniklu", 5, "uqaujjigiarutiniglu"),
	};
	
	@Test
	public void test__EvaluateSugestions__MostFrequentWords__AssumingWordIsInDict() 
			throws Exception {
		//
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
		String focusOnExample = null;
//		focusOnExample = "nigiani";
		
		int verbosity = 1;
		double expPercentFoundInTopN = 0.95;
		double tolerance = 0.01;
		double expAverageRank = 3.2;
		double avgRankTolerance = 0.1;

		evaluateCheckerOnExamples(getLargeDictChecker(), 
				examples_MostFrequenMisspelledWords, focusOnExample,
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance, 
				verbosity);
	}
	
	@Test @Ignore
	public void test__EvaluateSugestions__DEBUG_MostFrequentWords__UsingSmallCustomDictionary() throws Exception {
		//
		// This test is used only for Debugging purposes and is usually left 
		// @Ignored.
		//
		// It does the same thing as test
		//
		//   test__EvaluateSugestions__LargeDictionary
		//
		// except that it does it with a small dictionary.
		// As a result, it loads and runs much faster.
		//
		
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
		String focusOnExample = null;
//		String focusOnExample = "tamaini";

		SpellChecker checker = new SpellChecker(CompiledCorpusRegistry.emptyCorpusName);

		int verbosity = 1;
		double expPercentFoundInTopN = 0.6;
		double tolerance = 0.01;	
		double expAverageRank = 3.4;
		double avgRankTolerance = 0.1;		
		evaluateCheckerOnExamples(checker, 
				examples_MostFrequenMisspelledWords, focusOnExample, 
				expPercentFoundInTopN, tolerance,
				expAverageRank, avgRankTolerance,
				verbosity);
	}	

	@Test
	public void test__EvaluateSugestions__HandpickedExamples__LargeDictionary() throws Exception {
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
		String focusOnExample = null;
//		String focusOnExample = "pivagiijainiq";		
		
		int verbosity = 2;
		double expPercentFoundInTopN = 0.93;
		double tolerance = 0.01;
		double expAverageRank = 1.23;
		double avgRankTolerance = 0.93;

		evaluateCheckerOnExamples(getLargeDictChecker(), 
				examples_HandPickedMispelledWords, focusOnExample,
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance, 
				verbosity);
	}

	public void evaluateCheckerOnExamples(SpellChecker spellChecker, 
			SpellCheckerExample[] examples, String focusOnExample, 
			double expPercentFoundInTopN, double tolerance,
			double expAverageRank, double avgRankTolerance) throws Exception {
		evaluateCheckerOnExamples(spellChecker, 
				examples, focusOnExample, 
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance, null);
	}

	public void evaluateCheckerOnExamples(SpellChecker spellChecker, 
			SpellCheckerExample[] examples, String focusOnExample, 
			double expPercentFoundInTopN, double tolerance, 
			double expAverageRank, double avgRankTolerance,
			Integer verbosity) throws Exception {
		
		if (verbosity == null) verbosity = 0;
		//
		// For these tests, "pretend" that all the words from the 
		// examples were seen in the corpus used by the SpellChecker.
		//
		assumeCorrectionsAreInCheckerDict(examples, spellChecker);
		SpellCheckerEvaluator evaluator = new SpellCheckerEvaluator(spellChecker);
		evaluator.setVerbose(verbosity);
				
		for (SpellCheckerExample exampleData: examples) {
			if (focusOnExample == null || focusOnExample.equals(exampleData.wordToCheck)) {
				evaluator.onNewExample(exampleData);				
			}
		}
		
		// Used to be able to get > 0.87. Why has this
		// DECREASED eventhough I "improved" the suggestions 
		// scoring algorithm?
		int N = 5;
		assertEvaluationAsExpected(evaluator, N, expPercentFoundInTopN, tolerance,
				expAverageRank, avgRankTolerance);
		
	}	

	private void assertEvaluationAsExpected(SpellCheckerEvaluator evaluator, int N, double expPercentFoundInTopN,
			double tolerance, Double expAverageRank, Double avgRankTolerance) {
		
		String errMess = "";
		
		errMess += checkPercentInTopN(evaluator, N, expPercentFoundInTopN, tolerance);;
		errMess += checkAverageRank(evaluator, expAverageRank, avgRankTolerance);
		errMess += checkExamplesWithBadRank(evaluator);	
		
		if (!errMess.isEmpty()) {
			fail(errMess);
		}
	}


	private String checkPercentInTopN(SpellCheckerEvaluator evaluator, int N, 
			double expPercentFoundInTopN, double tolerance) {
			
		String errMess = "";
		
		List<Pair<Integer,Double>> histogram = evaluator.correctSpellingRankHistogramRelative();
		double gotPercentFoundInTopN = 0.0;
		for (int rankFound=0; rankFound < N; rankFound++) {
			if (histogram.size() > rankFound) {
				Pair<Integer,Double> histEntry = histogram.get(rankFound);
				gotPercentFoundInTopN += histEntry.getSecond();
			}
		}
		
		double delta = gotPercentFoundInTopN - expPercentFoundInTopN;
		if (Math.abs(delta) > tolerance) {
			if (delta < 0) {
				errMess = 
					"Significant DECREASE found for the percentage of words with an acceptable correction in the top "+N+
						"\n  Got: "+gotPercentFoundInTopN+"\n  Exp: "+expPercentFoundInTopN+"\n  Delta: "+delta
					;
			} else {
				errMess = 
					"Significant INCREASE found for the percentage of words with an acceptable correction in the top "+N+
						"\n  Got: "+gotPercentFoundInTopN+"\n  Exp: "+expPercentFoundInTopN+"\n  Delta: "+delta+
						"\n\nYou should probably change the expectations for that test so we don't loose that improvement in the future."
						;
			}
		}
		
		if (!errMess.isEmpty()) {
			errMess += "\n\n---------------\n\n";
			errMess = "\n"+errMess;
		}
		
		return errMess;
	}


	private String checkAverageRank(SpellCheckerEvaluator evaluator, Double expAverageRank, Double avgRankTolerance) {
		String errMess = "";
		
		double expMin = expAverageRank - avgRankTolerance;
		double expMax = expAverageRank + avgRankTolerance;
		if (evaluator.averageRank() == null) {
			errMess = "Average rank was null!!!";
		} else {
			if (evaluator.averageRank() > expMax) {
				errMess = 
					"The average rank was higher than expected.\n"+
					"  got: "+evaluator.averageRank()+
					" (exp <= "+expMax+")";
			} else if (evaluator.averageRank() < expMin) {
				errMess = 
					"Significant improvement found in the average rank.\n"+
					"You might want to decrease the expectation so we don't loose that gain in the future.\n"+
					"  got: "+evaluator.averageRank()+
					" (exp >= "+expMin+")";
			}
		}
		
		if (!errMess.isEmpty()) {
			errMess += "\n\n--------------------------\n\n";
		}
				
		return errMess;
	}


	private String checkExamplesWithBadRank(SpellCheckerEvaluator evaluator) {
		String errMess = "";
		if (evaluator.examplesWithBadRank.size() > 0) {
			errMess = 
				"There were examples for which the rank of the first correct suggestion exceeded the expected maximum.\n"+
				"List of such examples below.\n\n";
			for (SpellCheckerExample example: evaluator.examplesWithBadRank.keySet()) {
				Pair<Integer,List<String>> problem = 
						evaluator.examplesWithBadRank.get(example);
				String word = example.wordToCheck;
				Integer rank = problem.getFirst();
				List<String> topCandidates = 
						problem.getSecond()
							.stream()
							.limit(20)
							.collect(Collectors.toList());
				errMess += "  "+word+": rank="+rank+
					" (exp <= "+example.expMaxRank+")\n"+
					"  Correctly spelled forms: "+
					StringUtils.join(example.acceptableCorrections.iterator(), ", ")+"\n"+
					"  Top candidates were: "+
					StringUtils.join(topCandidates.iterator(), ", ")+"\n\n"
					;
			}
		}
		
		return errMess;
	}
	
	@Test
	public void test__firstPassCandidates_TFIDF__HandPickedExamples() 
			throws Exception {
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
//		String focusOnExample = null;
		String focusOnExample = "maliklugu";
		
		SpellChecker checker = getLargeDictChecker();
		
		for (SpellCheckerExample anExample: examples_HandPickedMispelledWords) {
			if (focusOnExample != null && 
					!focusOnExample.equals(anExample.key())) {
				continue;
			}
			
			
			String wordToCheck = anExample.wordToCheck;
			Set<String> gotCandidates = 
					checker.firstPassCandidates_TFIDF(wordToCheck, false);
			
			Set<Object> gotCandidatesObj = (Set)gotCandidates; 
			Set<Object> expCandidatesObj = (Set)anExample.acceptableCorrections;
//			Assert.fail("failed in test directly");
			AssertHelpers.intersectionNotEmpty(
				"\nThe first pass candidates for mis-spelled word '"+
				wordToCheck+"' did not contain any of the acceptable corrections.\n"+
				"Acceptable corrections were: ['"+String.join("', '", anExample.acceptableCorrections)+"]",
				gotCandidatesObj, expCandidatesObj);
		}
	}
	
	//////////////////////
	// TEST HELPERS
	//////////////////////
	
	/**
	 * The spell checker relies heavily on 
	 * of correct words that was compiled from a
	 * corpus. Such dictionaries take a long 
	 * time to compile, and sometimes, they are 
	 * missing some words that are needed for 
	 * the tests (because of a bug in that was 
	 * in the corpus compiler at the time we 
	 * compiled the corpus used for spell 
	 * checking).
	 * 
	 * Rather than wait for a new better version 
	 * of the corpus to be generated, we can use 
	 * this method to patch up the dictionary and 
	 * add words that are required by this a test 
	 * 
	 * @author desilets
	 *
	 */
	private void assumeCorrectionsAreInCheckerDict(SpellCheckerExample[] examples, SpellChecker spellChecker) {
		for (SpellCheckerExample anExample: examples) {
			for (String aCorrection: anExample.acceptableCorrections) {
				spellChecker.addCorrectWord(aCorrection);				
			}
		}
	}
}
