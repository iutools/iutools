/*
 * Created on Aug 19, 2004
 *
 * 
 */
package org.iutools.morph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeoutException;

import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.LinguisticDataException;
import ca.nrc.dtrc.stats.FrequencyHistogram;

/**
 * @author Marta
 *
 */
public class MorphologicalAnalyzer_AccuracyTest {
	
	boolean verbose = true;
	
	MorphologicalAnalyzer morphAnalyzer = null;

	MorphAnalGoldStandardAbstract goldStandard = null;
	MorphAnalCurrentExpectationsAbstract expectations = null;

	FrequencyHistogram<MorphAnalCurrentExpectationsAbstract.OutcomeType> gotOutcomeHist = new FrequencyHistogram<MorphAnalCurrentExpectationsAbstract.OutcomeType>();
	FrequencyHistogram<MorphAnalCurrentExpectationsAbstract.OutcomeType> expOutcomeHist = new FrequencyHistogram<MorphAnalCurrentExpectationsAbstract.OutcomeType>();
	
	/*
	 * @see TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		if (morphAnalyzer==null) {
			morphAnalyzer = new MorphologicalAnalyzer();
		}
		morphAnalyzer.activateTimeout();
		gotOutcomeHist = new FrequencyHistogram<MorphAnalCurrentExpectationsAbstract.OutcomeType>();
		expOutcomeHist = new FrequencyHistogram<MorphAnalCurrentExpectationsAbstract.OutcomeType>();
	}

	@Test
	public void test_accuracy_with_GoldStandard_Hansard() throws Exception {

		System.out.println("Running test_accuracy_with_GoldStandard_Hansard. This test can take a few minutes to complete.");

		goldStandard = new MorphAnalGoldStandard_Hansard();
		expectations = new MorphAnalCurrentExpectations_Hansard();

		test_accuracy();
	}

	@Test
	public void test_accuracy_with_GoldStandard_WordsThatFailedBefore() throws Exception {

		System.out.println("Running test_accuracy_with_GoldStandard_WordsThatFailedBefore. This test can take a few minutes to complete.");

		goldStandard = new MorphAnalGoldStandard_WordsThatFailedBefore();
		expectations = new MorphAnalCurrentExpectations_WordsThatFailedBefore();

		test_accuracy();
	}

	private void test_accuracy() throws Exception {
		//
		// Leave this at null to run on all words
		// Set it to a word if you want to run the tests just on that one word.
		//
		String focusOnWord = null;
//		focusOnWord = "ajjigiinngittunut";
		
		// Uncomment for debugging.
		morphAnalyzer.disactivateTimeout();

		Calendar startCalendar = Calendar.getInstance();
		
        Map<String,String> outcomeDifferences = new HashMap<>();
        
		for (String wordToBeAnalyzed: goldStandard.allWords()) {
		    AnalyzerCase caseData = goldStandard.caseData(wordToBeAnalyzed);

		    if (skipCase(caseData, focusOnWord)) {
		    	continue;
		    }

			if (verbose) System.out.print("> :"+wordToBeAnalyzed+":");
			
		    AnalysisOutcome outcome = decompose(wordToBeAnalyzed);
		    
            if (verbose) System.out.println(" []");
            
            checkOutcome(wordToBeAnalyzed, outcome, expectations, goldStandard, 
            		outcomeDifferences);
		}
		
		Calendar endCalendar = Calendar.getInstance();
		
		long time = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
		
        if (verbose) System.out.println("");
        if (verbose) System.out.println("Time in milliseconds: "+time);
        
        printPerformanceStats();
        
        assertOutcomesHaveNotChanged(outcomeDifferences);
        
        if (focusOnWord != null) {
        	Assert.fail("Test was only run on single word "+focusOnWord+
        		"\nDon't forget to reset focusOnWord=null before committing!");
        }
	}
	
	private void printPerformanceStats() {
        printOutcomeHistogram("Histogram of EXPECTED outcome types", expOutcomeHist);
        printOutcomeHistogram("Histogram of ACTUAL outcome types", gotOutcomeHist);
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

	private void assertOutcomesHaveNotChanged(Map<String, String> outcomeDifferences) {
		if (!outcomeDifferences.isEmpty()) {
			int nDiff = outcomeDifferences.keySet().size();
			
			List<String> failingWords = new ArrayList<String>();
			failingWords.addAll(outcomeDifferences.keySet());
			Collections.sort(failingWords);
			
			String failMess = 
				"There were "+nDiff+" differences in the analysis outcomes of some words.\n"+
				"Differences are listed below\n";
			for (String word: failingWords) {
				failMess += 
					"\n---------------------------------------\n\n"+
					"Word: "+word+"\n"+
					"    "+
					outcomeDifferences.get(word).replaceAll("\n", "\n    ")
					;
			}

			Assert.fail(failMess);
		}
	}

	private void checkOutcome(String word, AnalysisOutcome gotOutcome, 
		MorphAnalCurrentExpectationsAbstract expectations,
		MorphAnalGoldStandardAbstract goldStandard,
		Map<String, String> outcomeDiffs) {
		
		MorphAnalCurrentExpectationsAbstract.OutcomeType expOutcomeType = expectations.expectedOutcome(word);
		expOutcomeHist.updateFreq(expOutcomeType);
		
		String correctDecomp = goldStandard.correctDecomp(word);
		MorphAnalCurrentExpectationsAbstract.OutcomeType gotOutcomeType =
			expectations.type4outcome(gotOutcome, correctDecomp);
		gotOutcomeHist.updateFreq(gotOutcomeType);
		
		if (gotOutcomeType != expOutcomeType) {
			String diffMess = 
				diffMessage(expOutcomeType, gotOutcomeType, gotOutcome, 
						correctDecomp);
			logOutcomeDifference(word, diffMess, outcomeDiffs);
		}
	}
	
	private String diffMessage(MorphAnalCurrentExpectationsAbstract.OutcomeType expOutcomeType,
							   MorphAnalCurrentExpectationsAbstract.OutcomeType gotOutcomeType, AnalysisOutcome gotOutcome,
							   String correctDecomp) {
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
			mess += currentStateMessage(gotOutcome, correctDecomp);
		}
		
		return mess;
	}

	private String currentStateMessage(AnalysisOutcome gotOutcome, String correctDecomp) {
		String mess = 
			"\n" +
			"Correct decomp : "+correctDecomp+"\n"+
			"Got decomps:\n"+gotOutcome.joinDecomps();
		return mess;
	}

	private String worseningMessage(MorphAnalCurrentExpectationsAbstract.OutcomeType expOutcomeType) {
		String mess = null;
		if (expOutcomeType == MorphAnalCurrentExpectationsAbstract.OutcomeType.SUCCESS) {
			mess = "Correct decomposition used to be first in the list";
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
			outcome.decompositions = morphAnalyzer.decomposeWord(word);
		} catch (TimeoutException | MorphologicalAnalyzerException e) {
			outcome.timedOut = true;
		}
		
		return outcome;
	}

	private void echo(String mess) {
		System.out.println(mess);
	}
}
