/*
 * Created on Aug 19, 2004
 *
 * 
 */
package ca.inuktitutcomputing.morph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeoutException;

import ca.inuktitutcomputing.data.LinguisticData;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.MorphAnalCurrentExpectations.OutcomeType;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.dtrc.stats.FrequencyHistogram;

/**
 * @author Marta
 *
 */
public class DecomposeHansardTest {
	
	boolean verbose = true;
	
	MorphologicalAnalyzer morphAnalyzer = null;
	
	FrequencyHistogram<OutcomeType> gotOutcomeHist = new FrequencyHistogram<OutcomeType>();
	FrequencyHistogram<OutcomeType> expOutcomeHist = new FrequencyHistogram<OutcomeType>();
	
	/*
	 * @see TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		morphAnalyzer = new MorphologicalAnalyzer();
		LinguisticData.init();
		gotOutcomeHist = new FrequencyHistogram<OutcomeType>();
		expOutcomeHist = new FrequencyHistogram<OutcomeType>();
	}
	
	@Test
	public void testDecomposer() throws Exception {
				
		System.out.println("Running testDecomposer. This test can take a few minutes to complete.");
		
		//
		// Leave this at null to run on all words
		// Set it to a word if you want to run the tests just on that one word.
		//
		String focusOnWord = null;
//		focusOnWord = "itsivautaaq";
		
		morphAnalyzer = new MorphologicalAnalyzer();
		MorphAnalCurrentExpectations expectations = new MorphAnalCurrentExpectations();
		
		Calendar startCalendar = Calendar.getInstance();
		
		MorphAnalGoldStandard goldStandard = new MorphAnalGoldStandard();
        Map<String,String> outcomeDifferences = new HashMap<String,String>();
        
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
			FrequencyHistogram<OutcomeType> outcomeHist) {
		echo("\n== "+title+" ==\n");
		
		echo("Cases with:");
		{		
			echo("  First decomposition is correct            : "+
					outcomeHist.frequency(OutcomeType.SUCCESS)+" ("+
					outcomeHist.relativeFrequency(OutcomeType.SUCCESS, 1)+")");
			echo("  Corr. decomp. not in 1st place            : "+
					outcomeHist.frequency(OutcomeType.CORRECT_NOT_FIRST)+" ("+
					outcomeHist.relativeFrequency(OutcomeType.CORRECT_NOT_FIRST, 1)+")");
			echo("  Some decomps produced but not correct one : "+
					outcomeHist.frequency(OutcomeType.CORRECT_NOT_PRESENT)+" ("+
					outcomeHist.relativeFrequency(OutcomeType.CORRECT_NOT_PRESENT, 1)+")");
			echo("  No decomps produced at all                : "+
					outcomeHist.frequency(OutcomeType.NO_DECOMPS)+" ("+
					outcomeHist.relativeFrequency(OutcomeType.NO_DECOMPS, 1)+")");		
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
		MorphAnalCurrentExpectations expectations, 
		MorphAnalGoldStandard goldStandard, 
		Map<String, String> outcomeDiffs) {
		
		OutcomeType expOutcomeType = expectations.expectedOutcome(word);
		expOutcomeHist.updateFreq(expOutcomeType);
		
		String correctDecomp = goldStandard.correctDecomp(word);
		OutcomeType gotOutcomeType = 
			expectations.type4outcome(gotOutcome, correctDecomp);
		gotOutcomeHist.updateFreq(gotOutcomeType);
		
		if (gotOutcomeType != expOutcomeType) {
			String diffMess = 
				diffMessage(expOutcomeType, gotOutcomeType, gotOutcome, 
						correctDecomp);
			logOutcomeDifference(word, diffMess, outcomeDiffs);
		}
	}
	
	private String diffMessage(OutcomeType expOutcomeType, 
			OutcomeType gotOutcomeType, AnalysisOutcome gotOutcome, 
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

	private String worseningMessage(OutcomeType expOutcomeType) {
		String mess = null;
		if (expOutcomeType == OutcomeType.SUCCESS) {
			mess = "Correct decomposition used to be first in the list";
		} else if (expOutcomeType == OutcomeType.CORRECT_NOT_FIRST) {
			mess = "Correct decompositions used to be somewhere in the list";
		}
		return mess;
	}

	private String improvementMessage(OutcomeType expOutcomeType) {
		String mess = null;
		if (expOutcomeType == OutcomeType.CORRECT_NOT_FIRST) {
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
					caseData.skipped || 
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
