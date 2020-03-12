package ca.pirurvik.iutools.spellchecker;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.string.StringUtils;
import ca.pirurvik.iutools.CompiledCorpusRegistry;

public class SpellCheckerAccuracyTest {
	
	SpellChecker checkerLargeDict = null;
	
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	
	private static final SpellCheckerExample[] examplesForSuggestions = new SpellCheckerExample[] { 
		new SpellCheckerExample("nunavumi", 5, "nunavummi"),
		new SpellCheckerExample("immaqa", 5, "immaqaa"),
		new SpellCheckerExample("kiinaujatigut", 5, "kiinaujaqtigut"),
		new SpellCheckerExample("kiinaujat", 5, "kiinaujait"),
		new SpellCheckerExample("maligaliqtit", 5, "maligaliqtiit"),
		new SpellCheckerExample("nunavungmi", 5, "nunavummi"),
		new SpellCheckerExample("tamatuminnga", 5, "tamatuminga"),
		new SpellCheckerExample("katimmajjutiksaq", 5, "katimajjutiksaq"),
		new SpellCheckerExample("tanna", 5, "taanna"),
		new SpellCheckerExample("nunavuumit", 5, "nunavummit"),
		new SpellCheckerExample("nniaqtulirinirmut", 5, "aanniaqtulirinirmut"),
		new SpellCheckerExample("qallunaatitut", 5, "qallunaaqtitut"),
		new SpellCheckerExample("nakuqmi", 5, "nakurmiik"),
		new SpellCheckerExample("takkua", 5, "taakkua"),
		new SpellCheckerExample("nunavumiut", 5, "nunavummiut"),
		new SpellCheckerExample("nunavuumik", 5, "nunavummik"),
		new SpellCheckerExample("nunavutmi", 5, "nunavummi"),
		new SpellCheckerExample("asuillaak", 5, "asuilaak"),
		new SpellCheckerExample("pigiaqtitat", 5, "pigiaqtitait"),
		new SpellCheckerExample("uvalu", 5, "uvvalu"),
		new SpellCheckerExample("maligatigut", 5, "maligaqtigut"),
		new SpellCheckerExample("akitujutinut", 5, "akitujuutinut"),
		new SpellCheckerExample("arragumi", 5, "arraagumi"),
		new SpellCheckerExample("nniaqamangittulirinirmut", 5, "aanniaqamangittulirinirmut"),
		new SpellCheckerExample("nigiani", 5, "niggiani"),
		new SpellCheckerExample("tamakkuninnga", 5, "tamakkuninga"),
		new SpellCheckerExample("iksivautap", 5, "iksivautaup"),
		new SpellCheckerExample("sulikkanniiq", 5, "sulikkanniq"),
		new SpellCheckerExample("nunavumut", 5, "nunavummut"),
		new SpellCheckerExample("katimajit", 5, "katimajiit"),
		new SpellCheckerExample("tamatumunnga", 5, "tamatumunga"),
		new SpellCheckerExample("nniaqamangittulirijiit", 5, "aanniaqamangittulirijiit"),
		new SpellCheckerExample("ugaalautaa", 5, "uqaalautaa"),
		new SpellCheckerExample("tavani", 5, "tavvani"),
		new SpellCheckerExample("iksivauitaaq", 5, "iksivautaaq", "iksivautaak", "issivautaaq", "issivautaak", "itsivautaaq", "itsivautaak"),
		
		new SpellCheckerExample("tamaini", 5, "tamainni"),
		
		new SpellCheckerExample("nniaqtulirijikkunnut", 5, "aanniaqtulirijikkunnut"),
		new SpellCheckerExample("immaqaqai", 5, "immaqaaqai"),
		new SpellCheckerExample("taimak", 5, "taimaak")
	};
	
	private SpellChecker getLargeDictChecker() throws StringSegmenterException, SpellCheckerException {
		if (checkerLargeDict == null) {
			checkerLargeDict = new SpellChecker();
		}
		return checkerLargeDict;
	}

	@Test
	public void test__EvaluateSugestions__SmallCustomDictionary() throws Exception {
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
//		String focusOnExample = null;
		String focusOnExample = "tamaini";
		
		SpellChecker checker = new SpellChecker(CompiledCorpusRegistry.emptyCorpusName);
		evaluateSuggestions(checker, focusOnExample);
	}
	

	@Test
	public void test__EvaluateSugestions__LargeDictionary() throws Exception {
		// Set this to a specific example if you only want 
		// to evaluate that one.
		//
//		String focusOnExample = null;
		String focusOnExample = "tamaini";
		
		evaluateSuggestions(getLargeDictChecker(), focusOnExample);
	}
	
	public void evaluateSuggestions(SpellChecker spellChecker, String focusOnExample) throws Exception {
		//
		// For this test, "pretend" that all the words from the 
		// examples were seen in the corpus used by the SpellChecker.
		// This is because so
		assumeCorrectionsAreInCheckerDict(examplesForSuggestions, spellChecker);
		SpellCheckerEvaluator evaluator = new SpellCheckerEvaluator(spellChecker);
				
		for (SpellCheckerExample exampleData: examplesForSuggestions) {
			if (focusOnExample == null || focusOnExample.equals(exampleData.wordToCheck)) {
				evaluator.onNewExample(exampleData);				
			}
		}
		
		double expAverageRank = 1.17;
		double avgRankTolerance = 0.05;
		int N = 5;
		
		// Used to be able to get > 0.87. Why has this
		// DECREASED eventhough I "improved" the suggestions 
		// scoring algorithm?
//		double expPercentFoundInTopN = 0.87;
		double expPercentFoundInTopN = 0.6;
		double tolerance = 0.01;		
		assertEvaluationAsExpected(evaluator, N, expPercentFoundInTopN, tolerance,
				expAverageRank, avgRankTolerance);
		
		Assert.fail("All current test expectations have been met, but those expectations are too low. See if we can improve the algorithm so it can meet higher expectations");
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
