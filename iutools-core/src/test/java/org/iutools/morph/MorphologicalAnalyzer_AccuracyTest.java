/*
 * Created on Aug 19, 2004
 *
 * 
 */
package org.iutools.morph;

import ca.nrc.testing.AssertNumber;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeoutException;

import org.iutools.linguisticdata.LinguisticDataException;
import ca.nrc.dtrc.stats.FrequencyHistogram;

import org.iutools.morph.MorphAnalCurrentExpectationsAbstract.OutcomeType;

/**
 * @author Marta
 *
 */
public class MorphologicalAnalyzer_AccuracyTest {

	boolean verbose = false;
	
	MorphologicalAnalyzer morphAnalyzer = null;

	MorphAnalGoldStandardAbstract goldStandard = null;
	MorphAnalCurrentExpectationsAbstract expectations = null;

	FrequencyHistogram<OutcomeType> gotOutcomeHist =
		new FrequencyHistogram<OutcomeType>();
	FrequencyHistogram<OutcomeType> expOutcomeHist =
		new FrequencyHistogram<OutcomeType>();

	/*
	 * @see TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		if (morphAnalyzer==null) {
			// check how much time it takes for the analyzer to be created (in fact, this is the time for loading the database)
			Calendar startCalendar = Calendar.getInstance();
			morphAnalyzer = new MorphologicalAnalyzer();
			Calendar endCalendar = Calendar.getInstance();

			long time = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();

			System.out.println("");
			System.out.println("creating new MorphologicalAnalyzer: Time in milliseconds: "+time);
		}
		morphAnalyzer.activateTimeout();
		gotOutcomeHist = new FrequencyHistogram<OutcomeType>();
		expOutcomeHist = new FrequencyHistogram<OutcomeType>();
	}

	@Test
	public void test_accuracy_with_GoldStandard_Hansard() throws Exception {

		System.out.println("Running test_accuracy_with_GoldStandard_Hansard.");

		goldStandard = new MorphAnalGoldStandard_Hansard();
		expectations = new MorphAnalCurrentExpectations_Hansard();

		// If you want to only evaluate one word, comment out and modify the
		// next line.
//		expectations.focusOnWord = "someword";

		evaluateAccuracy();
	}

	@Test
	public void test_accuracy_with_GoldStandard_WordsThatFailedBefore() throws Exception {

		System.out.println("Running test_accuracy_with_GoldStandard_WordsThatFailedBefore.");

		goldStandard = new MorphAnalGoldStandard_WordsThatFailedBefore();
		expectations = new MorphAnalCurrentExpectations_WordsThatFailedBefore();

		evaluateAccuracy();
	}

	private void evaluateAccuracy() throws Exception {

		System.out.println("This test can take a few minutes to complete.");

		// Uncomment for debugging.
		morphAnalyzer.disactivateTimeout();

		Calendar startCalendar = Calendar.getInstance();

		Map<String,String> outcomeDifferences = new HashMap<>();

		int column = 0;
		for (String wordToBeAnalyzed: goldStandard.allWords()) {
			 column++;
		    AnalyzerCase caseData = goldStandard.caseData(wordToBeAnalyzed);

			if (verbose) {
				System.out.print("> :"+wordToBeAnalyzed+":");
			} else {
				System.out.print(".");
				if (column == 80) {
					System.out.print("\n");
					column = 0;
				}
			}

		    if (skipCase(caseData, expectations.focusOnWord)) {
		    	continue;
		    }

		    AnalysisOutcome outcome = decompose(wordToBeAnalyzed);
		    
            if (verbose) System.out.println(" []");
            
            checkOutcome(wordToBeAnalyzed, outcome, expectations, goldStandard, 
            		outcomeDifferences);
		}
		
		Calendar endCalendar = Calendar.getInstance();
		
		long time = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
		
//        if (verbose)
        	System.out.println("");
//        if (verbose)
        	System.out.println("Analysis of all words: Time in milliseconds: "+time);
        
        printPerformanceStats();
        
        assertOutcomesHaveNotChangedSignificantly(outcomeDifferences);
        
        if (expectations.focusOnWord != null) {
        	Assert.fail("Test was only run on single word "+expectations.focusOnWord+
        		"\nDon't forget to reset focusOnWord=null before committing!");
        }
	}
	
	private void printPerformanceStats() {
		System.out.println();
		printCorrectFoundStats();
		printOutcomeHistogram("Histogram of EXPECTED outcome types", expOutcomeHist);
		printOutcomeHistogram("Histogram of ACTUAL outcome types", gotOutcomeHist);
	}

	private void printCorrectFoundStats() {
		long totalWords = gotOutcomeHist.totalOccurences();

		// Words where decomps were produced, but they were all incorrect
		long totalNotPresent = gotOutcomeHist.frequency(OutcomeType.CORRECT_NOT_PRESENT);

		// Words where no decomps were produced at all
		long totalNoDecomps = gotOutcomeHist.frequency(OutcomeType.NO_DECOMPS);

		// Words where decomps were produced, and one of them was correct
		long totalCorrectPresent = totalWords - (totalNoDecomps + totalNotPresent);
		Double correctRate = 1.0 * totalCorrectPresent / totalWords;


		System.out.println("\nWords with correct decomp found: "+
			totalCorrectPresent+"/"+totalWords+" (rate: "+correctRate+")");
	}

	private void printOutcomeHistogram(String title,
		FrequencyHistogram<MorphAnalCurrentExpectationsAbstract.OutcomeType> outcomeHist) {
		echo("\n== "+title+" ==\n");
		
		echo("Cases with:");
		{		
			echo("  First decomposition is correct            : "+
					outcomeHist.frequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.SUCCESS)+" ("+
					outcomeHist.relativeFrequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.SUCCESS, 1)+")");
			echo("  Corr. decomp. not in 1st place            : "+
					outcomeHist.frequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.CORRECT_NOT_FIRST)+" ("+
					outcomeHist.relativeFrequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.CORRECT_NOT_FIRST, 1)+")");
			echo("  Some decomps produced but not correct one : "+
					outcomeHist.frequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.CORRECT_NOT_PRESENT)+" ("+
					outcomeHist.relativeFrequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.CORRECT_NOT_PRESENT, 1)+")");
			echo("  No decomps produced at all                : "+
					outcomeHist.frequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.NO_DECOMPS)+" ("+
					outcomeHist.relativeFrequency(MorphAnalCurrentExpectationsAbstract.OutcomeType.NO_DECOMPS, 1)+")");
		}
	}

	private void assertOutcomesHaveNotChangedSignificantly(
		Map<String, String> outcomeDifferences) {

		String failMess = significantChangesMessage();

		if (!failMess.isEmpty() && !outcomeDifferences.isEmpty()) {
			int nDiff = outcomeDifferences.keySet().size();

			List<String> failingWords = new ArrayList<String>();
			failingWords.addAll(outcomeDifferences.keySet());
			Collections.sort(failingWords);

			failMess +=
				"Below are the " + nDiff + " words for which there were differences between the expected and achieved outcome.\n";
			for (String word : failingWords) {
				failMess +=
				"\n---------------------------------------\n\n" +
				"Word: " + word + "\n" +
				"    " +
				outcomeDifferences.get(word).replaceAll("\n", "\n    ")
				;
			}
		}
		if (!failMess.isEmpty()) {
			Assert.fail(failMess);
		}
	}

	private String significantChangesMessage() {
		String mess = "";

		Double gotValue = null;
		Double expValue = null;
		Double tolerance = null;

		try {
			gotValue = 1.0 * gotOutcomeHist.frequency(OutcomeType.NO_DECOMPS);
			expValue = 1.0 * expOutcomeHist.frequency(OutcomeType.NO_DECOMPS);
			tolerance = expValue * expectations.tolerance_NO_DECOMPS;
			AssertNumber.performanceHasNotChanged(
				"Words that do not produce any decomps",
				gotValue, expValue, tolerance, false);
		} catch (AssertionError e) {
			mess += "\n"+e.getMessage();
		}

		try {
			gotValue = 1.0 * gotOutcomeHist.frequency(OutcomeType.CORRECT_NOT_PRESENT);
			expValue = 1.0 * expOutcomeHist.frequency(OutcomeType.CORRECT_NOT_PRESENT);
			tolerance = expValue * expectations.tolerance_CORRECT_NOT_PRESENT;
			AssertNumber.performanceHasNotChanged(
				"Words that do not produce any decomps",
				gotValue, expValue, tolerance, false);
		} catch (AssertionError e) {
			mess += "\n"+e.getMessage();
		}

		try {
			gotValue = 1.0 * gotOutcomeHist.frequency(OutcomeType.CORRECT_NOT_FIRST);
			expValue = 1.0 * expOutcomeHist.frequency(OutcomeType.CORRECT_NOT_FIRST);
			tolerance = expValue * expectations.tolerance_CORRECT_NOT_FIRST;
			AssertNumber.performanceHasNotChanged(
				"Words where the first decomp is not correct",
				gotValue, expValue, tolerance, false);
		} catch (AssertionError e) {
			mess += "\n"+e.getMessage();
		}

		return mess;
	}

	private void checkOutcome(String word, AnalysisOutcome gotOutcome, 
		MorphAnalCurrentExpectationsAbstract expectations,
		MorphAnalGoldStandardAbstract goldStandard,
		Map<String, String> outcomeDiffs) throws Exception {
		
		OutcomeType expOutcomeType = expectations.expectedOutcome(word);
		expOutcomeHist.updateFreq(expOutcomeType);

		String[] correctDecomps = goldStandard.correctDecomps(word);
		OutcomeType gotOutcomeType =
			expectations.type4outcome(gotOutcome, correctDecomps);
		gotOutcomeHist.updateFreq(gotOutcomeType);

		if (gotOutcomeType != expOutcomeType) {
			String diffMess = 
				diffMessage(expOutcomeType, gotOutcomeType, gotOutcome, 
						correctDecomps);
			logOutcomeDifference(word, diffMess, outcomeDiffs);
		}
	}

	private String diffMessage(OutcomeType expOutcomeType,
		OutcomeType gotOutcomeType, AnalysisOutcome gotOutcome,
		String[] correctDecomps) {
		String mess = "";
		int comp = gotOutcomeType.compareTo(expOutcomeType);
		boolean improved = false;
		if (gotOutcomeType.compareTo(expOutcomeType) > 0) {
			improved = true;
		}
		
		if (improved) {
			mess += "GOOD NEWS\n";	
			mess += improvementMessage(expOutcomeType);
		} else {
			mess += "BAD NEWS\n";
			mess += worseningMessage(expOutcomeType);
			mess += currentStateMessage(gotOutcome, correctDecomps);
		}
		
		return mess;
	}

	private String currentStateMessage(AnalysisOutcome gotOutcome,
		String[] correctDecomps) {
		String mess = 
			"\n" +
			"Correct decomps :\n  "+String.join("\n  ", correctDecomps)+"\n"+
			"Got decomps:\n"+gotOutcome.joinDecomps();
		return mess;
	}

	private String worseningMessage(MorphAnalCurrentExpectationsAbstract.OutcomeType expOutcomeType) {
		String mess = null;
		if (expOutcomeType == MorphAnalCurrentExpectationsAbstract.OutcomeType.SUCCESS) {
			mess = "First decomposition used to be correct";
		} else if (expOutcomeType == MorphAnalCurrentExpectationsAbstract.OutcomeType.CORRECT_NOT_FIRST) {
			mess = "Correct decompositions used to be somewhere in the list";
		}
		return mess;
	}

	private String improvementMessage(MorphAnalCurrentExpectationsAbstract.OutcomeType expOutcomeType) {
		String mess = null;
		if (expOutcomeType == MorphAnalCurrentExpectationsAbstract.OutcomeType.CORRECT_NOT_FIRST) {
			mess = "Correct decomposition is now first in the list.";
		}
		
		return mess;
	}

	private void logOutcomeDifference(String word, String diffMess, 
			Map<String, String> outcomeDifferences) {		
		outcomeDifferences.put(word, diffMess);
	}

	private boolean skipCase(AnalyzerCase caseData, String focusOnWord) {
		Boolean skip = null;
		
		if (focusOnWord != null  && !focusOnWord.equals(caseData.word)) {
			skip = true;
		}
		
		if (skip == null) {
			if (caseData.isMisspelled || caseData.possiblyMisspelled ||
					caseData.properName || 
					caseData.isBorrowed ||
					caseData.decompUnknown) {
				skip = true;
			}
		}
		
		if (skip == null) {
			skip = false;
		}
		
		return skip;
	}

	private AnalysisOutcome decompose(String word) throws MorphInukException, LinguisticDataException {
		AnalysisOutcome outcome = new AnalysisOutcome();
		
		try {
			// AD-2020-05-13: Does this help ensure that timeout works?
			//
			if (morphAnalyzer==null) {
				morphAnalyzer = new MorphologicalAnalyzer();
			}
			outcome.decompositions = morphAnalyzer.decomposeWord_NEW(word);
		} catch (TimeoutException | MorphologicalAnalyzerException | DecompositionExcepion e) {
			outcome.timedOut = true;
		}
		
		return outcome;
	}

	private void echo(String mess) {
		System.out.println(mess);
	}
}
