package ca.pirurvik.iutools.spellchecker;

import static org.junit.Assert.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertNumber;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;

public abstract class SpellCheckerAccuracyTest {

	protected abstract SpellChecker makeLargeDictChecker() throws Exception;
	protected abstract SpellChecker makeEmptyDictChecker() throws Exception;

	SpellChecker checkerLargeDict = null;
	
	private static DecimalFormat df2 = new DecimalFormat("#.##");

	protected String usingCorpus() {
		return "Hansard1999-2002";
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
					
				// NEED-IMPROVEMENT: Examples with ranking > 5, 
				//   EVEN if we assume the correction is in the dict
				//

				new SpellCheckerExample("nakuqmi")
					.isMisspelled("nakurmiik").setMaxRank(6),

				new SpellCheckerExample("nunavungmi")
					.isMisspelled("nunavummi").setMaxRank(12),

				new SpellCheckerExample("nunavuumik")
					.isMisspelled("nunavummik").setMaxRank(26),

				new SpellCheckerExample("nunavuumit")
					.isMisspelled("nunavummit").setMaxRank(37),

				new SpellCheckerExample("ugaalautaa")
					.isMisspelled("uqaalautaa").setMaxRank(6),
				
				// NEED-IMPROVEMENT: Examples with ranking > 5
				//   ONLY if we don't assume the correction is in dict

				new SpellCheckerExample("qallunaatitut")
					.isMisspelled("qallunaaqtitut").setMaxRank(5, -1),

				new SpellCheckerExample("tamaini")
					.isMisspelled("tamainni").setMaxRank(5, -1),

				new SpellCheckerExample("nniaqamangittulirijiit")
					.isMisspelled("aanniaqamangittulirijiit").setMaxRank(5, -1),

				new SpellCheckerExample("nniaqamangittulirinirmut")
					.isMisspelled("aanniaqamangittulirinirmut").setMaxRank(5, -1),
				
									
				// OK: Examples with ranking <= 5

				new SpellCheckerExample("akitujutinut")
					.isMisspelled("akitujuutinut").setMaxRank(5),

				new SpellCheckerExample("arragumi")
					.isMisspelled("arraagumi").setMaxRank(5),

				new SpellCheckerExample("asuillaak")
					.isMisspelled("asuilaak").setMaxRank(5),

				new SpellCheckerExample("iksivauitaaq")
					.isMisspelled("iksivautaaq", "iksivautaak", "issivautaaq", "issivautaak", "itsivautaaq", "itsivautaak").setMaxRank(5),

				new SpellCheckerExample("iksivautap")
					.isMisspelled("iksivautaup").setMaxRank(5),

				new SpellCheckerExample("immaqa")
					.isMisspelled("immaqaa").setMaxRank(5),

				new SpellCheckerExample("katimajit")
					.isMisspelled("katimajiit").setMaxRank(5),

				new SpellCheckerExample("katimmajjutiksaq")
					.isMisspelled("katimajjutiksaq").setMaxRank(5),

				new SpellCheckerExample("kiinaujatigut")
					.isMisspelled("kiinaujaqtigut").setMaxRank(5),

				new SpellCheckerExample("kiinaujat")
					.isMisspelled("kiinaujait").setMaxRank(5),

				new SpellCheckerExample("maligaliqtit")
					.isMisspelled("maligaliqtiit").setMaxRank(5),

				new SpellCheckerExample("maligatigut")
					.isMisspelled("maligaqtigut").setMaxRank(5),

				new SpellCheckerExample("nigiani")
					.isMisspelled("niggiani").setMaxRank(5),

				new SpellCheckerExample("nniaqtulirinirmut")
					.isMisspelled("aanniaqtulirinirmut").setMaxRank(5),

				new SpellCheckerExample("nunavumi")
					.isMisspelled("nunavummi").setMaxRank(5),

				new SpellCheckerExample("nunavumiut")
					.isMisspelled("nunavummiut").setMaxRank(5),

				new SpellCheckerExample("nunavumut")
					.isMisspelled("nunavummut").setMaxRank(5),

				new SpellCheckerExample("nunavutmi")
					.isMisspelled("nunavummi").setMaxRank(5),

				new SpellCheckerExample("pigiaqtitat")
					.isMisspelled("pigiaqtitait").setMaxRank(5),

				new SpellCheckerExample("sulikkanniiq")
					.isMisspelled("sulikkanniq").setMaxRank(5),

				new SpellCheckerExample("takkua")
					.isMisspelled("taakkua").setMaxRank(5),

				new SpellCheckerExample("tamakkuninnga")
					.isMisspelled("tamakkuninga").setMaxRank(5),

				new SpellCheckerExample("tamatuminnga")
					.isMisspelled("tamatuminga").setMaxRank(5),

				new SpellCheckerExample("tamatumunnga")
					.isMisspelled("tamatumunga").setMaxRank(5),

				new SpellCheckerExample("tanna")
					.isMisspelled("taanna").setMaxRank(5),

				new SpellCheckerExample("tavani")
					.isMisspelled("tavvani").setMaxRank(5),

				new SpellCheckerExample("uvalu")
					.isMisspelled("uvvalu").setMaxRank(5),

				new SpellCheckerExample("nniaqtulirijikkunnut")
					.isMisspelled("aanniaqtulirijikkunnut").setMaxRank(5),

				new SpellCheckerExample("immaqaqai")
					.isMisspelled("immaqaaqai").setMaxRank(5),

				new SpellCheckerExample("taimak")
					.isMisspelled("taimaak").setMaxRank(5),
	};
		
	//
	// These examples were collected from a small set of handpicked 
	// web pages in Inuktut.
	//
	// For each page, we collected all the words tagged as errors by the
	// Spell Checker and verified them to the best of our ability.
	//	
	private static final SpellCheckerExample[] 
			examples_RandomPageSample = new SpellCheckerExample[] {
				// NEEDS-IMPROVEMENT: False positives
				//   i.e. words that the spell checker tags as mis-spelled 
				//   eventhough they are correctly spelled.
				//
				new SpellCheckerExample("juusi")
					.notMisspelled(),
					
				new SpellCheckerExample("siqinnisiurnimut")
					.notMisspelled(),

				new SpellCheckerExample("pigiannganirmut")
					.notMisspelled(),

				new SpellCheckerExample("uppiritsiatatinnik")
					.notMisspelled(),

				new SpellCheckerExample("nunalipaujakkut")
					.notMisspelled(),

				new SpellCheckerExample("pijiraqtitsinasuaqpattuni")
					.notMisspelled(),

				new SpellCheckerExample("katujjiqatigiigutaujumi")
					.notMisspelled(),

				new SpellCheckerExample("katujjiqatigiigutaujuq")
					.notMisspelled(),

				new SpellCheckerExample("amisurukkanniqullugit")
					.notMisspelled(),

				new SpellCheckerExample("aup")
					.notMisspelled(),

				new SpellCheckerExample("pirajangniuvuq")
					.notMisspelled(),

				new SpellCheckerExample("pirajangnirmut")
					.notMisspelled(),

				new SpellCheckerExample("pirajangniujuni")
					.notMisspelled(),

				new SpellCheckerExample("pirajaktailimanirmut")
					.notMisspelled(),

				new SpellCheckerExample("qanutigi")
					.notMisspelled(),

				new SpellCheckerExample("nalunaikkutanganni")
					.notMisspelled(),

				new SpellCheckerExample("pasijaujumik")
					.notMisspelled(),

				new SpellCheckerExample("qaujinasuarningani")
					.notMisspelled(),

				new SpellCheckerExample("saanngajunnaqpuq")
					.notMisspelled(),

				new SpellCheckerExample("akiliititaunajarningani")
					.isMisspelled(),

				new SpellCheckerExample("anullaksirnirmut")
					.notMisspelled(),

				new SpellCheckerExample("aquttunnailliniujumik")
					.notMisspelled(),

					
				// NEEDS-IMPROVEMENT: Examples for which we only know
				//   whether or not they are mis-spelled (need to 
				//   figure out what the correct spellings are)
					
				new SpellCheckerExample("angijuqqaaqaqtutik")
					.isMisspelled(),

				new SpellCheckerExample("kanatilimaamik")
					.isMisspelled(),
					
				new SpellCheckerExample("suulur")
					.isMisspelled(),

				new SpellCheckerExample("ujaranniarvimmi")
					.isMisspelled(),
					

				// NEEDS-IMPROVEMENT: rank > 5 or null
				new SpellCheckerExample("piliriqatigiinik")
						.isMisspelled("piliriqatigiinnik").setMaxRank(null),
				
				// OK: rank <= 5
				new SpellCheckerExample("aanniaqarnngittulirijikkut")
						.isMisspelled("aanniaqanngittulirijikkut").setMaxRank(5),
					
				new SpellCheckerExample("angijuqqaaqaqtutik").setMaxRank(5)
						.isMisspelled("angajuqqaaqaqtutik"),	
						
				new SpellCheckerExample("maliklugu").setMaxRank(5)
						.isMisspelled("maliglugu"),
						
				new SpellCheckerExample("pivagiijainiq").setMaxRank(5)
						.isMisspelled("pivagiijarniq"),
						
				new SpellCheckerExample("qassigasangnut").setMaxRank(5)
						.isMisspelled("qassigalangnut"),
						
				new SpellCheckerExample("qaujisarutinginniklu").setMaxRank(5)
						.isMisspelled("qaujisarutinginniglu"),
						
				new SpellCheckerExample("qaritaujarmuaqtiqtaujuni").setMaxRank(5)
						.isMisspelled("qaritaujamuaqtitaujuni"),
						
				new SpellCheckerExample("silataaniingaaqtulirinirmut").setMaxRank(5)
						.isMisspelled("silataaninngaaqtulirinirmut"),
						
				new SpellCheckerExample("sivunnganit").setMaxRank(5)
						.isMisspelled("sivuninganit", "sivurnganit"),
						
				new SpellCheckerExample("tukimuaktittiniaqtumik").setMaxRank(5)
						.isMisspelled("tukimuaqtittiniaqtumik"),
						
				new SpellCheckerExample("tukimuaktiungmata").setMaxRank(5)
						.isMisspelled("tukimuaqtiungmata"),
						
				new SpellCheckerExample("upalungaijanirmut").setMaxRank(5)
						.isMisspelled("upalungaijarnirmut"),
						
				new SpellCheckerExample("uqaujjigiarutiniklu").setMaxRank(5)
						.isMisspelled("uqaujjigiarutiniglu"),
	};
	
	@Test
	public void test__Evaluate__MostFrequentMisspelledWords__AssumingWordIsInDict() 
			throws Exception {
		//
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
		String focusOnExample = null;
//		focusOnExample = "ugaalautaa";
		
		int verbosity = 1;
		double expPercentFoundInTopN = 0.97;
		double tolerance = 0.01;
		double expAverageRank = 1.35;
		double avgRankTolerance = 0.1;
		Boolean loadCorrectWordInDict = true;

		evaluateCheckerOnExamples(makeLargeDictChecker(),
				examples_MostFrequenMisspelledWords, focusOnExample,
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance, 
				loadCorrectWordInDict, verbosity);
	}
	
	@Test
	public void test__Evaluate__MostFrequentMisspelledWords__WIHOUT_AssumingWordIsInDict() 
			throws Exception {
		//
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
		String focusOnExample = null;
//		focusOnExample = "nigiani";
		
		int verbosity = 1;
		double expPercentFoundInTopN = 0.97;
		double tolerance = 0.01;
		double expAverageRank = 1.4;
		double avgRankTolerance = 0.1;
		Boolean loadCorrectWordInDict = false;

		evaluateCheckerOnExamples(makeLargeDictChecker(),
				examples_MostFrequenMisspelledWords, focusOnExample,
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance, 
				loadCorrectWordInDict, verbosity);
	}	
	@Ignore @Test
	public void test__Evaluate__DEBUG_MostFrequentWords__UsingSmallCustomDictionary() throws Exception {
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

		SpellChecker checker = makeEmptyDictChecker();

		int verbosity = 1;
		double expPercentFoundInTopN = 0.6;
		double tolerance = 0.01;	
		double expAverageRank = 3.4;
		double avgRankTolerance = 0.1;
		Boolean loadCorrectWordInDict = true;
		evaluateCheckerOnExamples(checker, 
				examples_MostFrequenMisspelledWords, focusOnExample, 
				expPercentFoundInTopN, tolerance,
				expAverageRank, avgRankTolerance,
				loadCorrectWordInDict, verbosity);
	}	

	@Test
	public void test__Evaluate__RandomPageSample__LargeDictionary() throws Exception {
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
		String focusOnExample = null;
//		focusOnExample = "angijuqqaaqaqtutik";		
		
		int verbosity = 2;
		double expPercentFoundInTopN = 0.93;
		double tolerance = 0.01;
		double expAverageRank = 1.23;
		double avgRankTolerance = 0.93;
		Boolean loadCorrectWordInDict = true;
		Double expFPRate = 0.52;
		Double toleranceFPRate = 0.01;
		
		// We should not get ANY False Negatives
		Double expFNRate = 0.0;
		Double toleranceFNRate = 0.01;

		evaluateCheckerOnExamples(makeLargeDictChecker(),
				examples_RandomPageSample, focusOnExample,
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance, 
				loadCorrectWordInDict, verbosity,
				expFPRate, toleranceFPRate,
				expFNRate, toleranceFNRate);
	}

	public void evaluateCheckerOnExamples(SpellChecker spellChecker, 
			SpellCheckerExample[] examples, String focusOnExample, 
			double expPercentFoundInTopN, double tolerance,
			double expAverageRank, double avgRankTolerance) throws Exception {
		evaluateCheckerOnExamples(spellChecker, 
				examples, focusOnExample, 
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance, null, null);
	}

	public void evaluateCheckerOnExamples(SpellChecker spellChecker, 
			SpellCheckerExample[] examples, String focusOnExample, 
			double expPercentFoundInTopN, double tolerance, 
			double expAverageRank, double avgRankTolerance,
			Boolean loadCorrectWordInDict, Integer verbosity) throws Exception {
		
		evaluateCheckerOnExamples(spellChecker, 
				examples, focusOnExample, 
				expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance,
				loadCorrectWordInDict, verbosity,
				null, null, null, null);
	}	
	
	private void evaluateCheckerOnExamples(SpellChecker spellChecker, 
			SpellCheckerExample[] examples, String focusOnExample, 
			double expPercentFoundInTopN, double tolerance, 
			double expAverageRank, double avgRankTolerance,
			Boolean loadCorrectWordInDict, Integer verbosity,
			Double expFPRate, Double toleranceFPRate,
			Double expFNRate, Double toleranceFNRate)  throws Exception {
		
		if (verbosity == null) verbosity = 0;
		if (loadCorrectWordInDict == null) loadCorrectWordInDict = false;
		
		//
		// For these tests, "pretend" that all the words from the 
		// examples were seen in the corpus used by the SpellChecker.
		//
		if (loadCorrectWordInDict) {
			assumeCorrectionsAreInCheckerDict(examples, spellChecker);
		}
		SpellCheckerEvaluator evaluator = new SpellCheckerEvaluator(spellChecker);
		evaluator.setVerbose(verbosity);
				
		for (SpellCheckerExample exampleData: examples) {
			if (focusOnExample == null || focusOnExample.equals(exampleData.wordToCheck)) {
				evaluator.onNewExample(exampleData, loadCorrectWordInDict);				
			}
		}
		
		int N = 5;
		assertEvaluationAsExpected(evaluator, N, 
				expPercentFoundInTopN, tolerance,
				expAverageRank, avgRankTolerance,
				expFPRate, toleranceFPRate,
				expFNRate, toleranceFNRate);
	}
	
	private void assertEvaluationAsExpected(SpellCheckerEvaluator evaluator, 
			int N, double expPercentFoundInTopN, double tolerance, 
			Double expAverageRank, Double avgRankTolerance) {
	
		assertEvaluationAsExpected(evaluator, 
				N, expPercentFoundInTopN, tolerance, 
				expAverageRank, avgRankTolerance);
	}

	private void assertEvaluationAsExpected(SpellCheckerEvaluator evaluator, 
			int N, double expPercentFoundInTopN, double tolerance, 
			Double expAverageRank, Double avgRankTolerance, 
			Double expFPRate,Double toleranceFPRate,
			Double expFNRate, Double toleranceFNRate) {
		
		String errMess = "";
		
		errMess += checkFalsePositiveRate(evaluator, expFPRate, toleranceFPRate);
		errMess += checkFalseNegativeRate(evaluator, expFNRate, toleranceFNRate);
		errMess += checkPercentInTopN(evaluator, N, expPercentFoundInTopN, tolerance);;
		errMess += checkAverageRank(evaluator, expAverageRank, avgRankTolerance);
		errMess += checkExamplesWithBadRank(evaluator);	
		
		if (!errMess.isEmpty()) {
			fail(errMess);
		}		
	}

	private String checkFalsePositiveRate(SpellCheckerEvaluator evaluator, 
			Double expFPRate, Double toleranceFPRate) {
		
		String errMess = "";
		if (expFPRate != null) {
			Double gotFPRate = evaluator.falsePositiveRate();
			try {
				AssertNumber.performanceHasNotChanged("False Positive Rate", 
					gotFPRate, expFPRate, toleranceFPRate,
					false);
			} catch (AssertionError e) {
				errMess = e.getMessage();
			}
		}
		return errMess;
	}

	private String checkFalseNegativeRate(SpellCheckerEvaluator evaluator, 
			Double expFNRate, Double toleranceFNRate) {
		
		String errMess = "";
		if (expFNRate != null) {
			Double gotFNRate = evaluator.falseNegativeRate();
			try {
				AssertNumber.performanceHasNotChanged("False Negative Rate", 
					gotFNRate, expFNRate, toleranceFNRate,
					false);
			} catch (AssertionError e) {
				errMess = e.getMessage();
			}
		}
		return errMess;
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
					" (exp <= "+example.maxRankAssumingInDict+")\n"+
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
	public void test__firstPassCandidates_TFIDF__HandPickedExamples__WITHOUT_AssumingCorrectSpellingInDict() 
			throws Exception {
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
//		String focusOnExample = null;
		String focusOnExample = "maliklugu";
		
		SpellChecker checker = makeLargeDictChecker();
		
		for (SpellCheckerExample anExample: examples_RandomPageSample) {
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
	 * @throws SpellCheckerException 
	 *
	 */
	private void assumeCorrectionsAreInCheckerDict(SpellCheckerExample[] examples, SpellChecker spellChecker) throws SpellCheckerException {
		for (SpellCheckerExample anExample: examples) {
			for (String aCorrection: anExample.acceptableCorrections) {
				spellChecker.addCorrectWord(aCorrection);				
			}
		}
	}
}
