/*
 * Created on Aug 19, 2004
 *
 * 
 */
package ca.inuktitutcomputing.morph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

import ca.inuktitutcomputing.data.LinguisticData;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.MorphAnalCurrentExpectations.OutcomeType;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;

/**
 * @author Marta
 *
 */
public class DecomposeHansardTest {
	
	boolean verbose = true;
	
	MorphologicalAnalyzer morphAnalyzer = null;
	Map<OutcomeType,Integer> outcomeHist = null;
	
	/*
	 * @see TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		morphAnalyzer = new MorphologicalAnalyzer();
		LinguisticData.init();	
		outcomeHist = new HashMap<OutcomeType,Integer>();
		for (OutcomeType type: OutcomeType.values()) {
			outcomeHist.put(type, new Integer(0));
		}
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
        
        printOutcomeHistogram();
        
        assertOutcomesHaveNotChanged(outcomeDifferences);
        
        if (focusOnWord != null) {
        	Assert.fail("Test was only run on single word "+focusOnWord+
        		"\nDon't forget to reset focusOnWord=null before committing!");
        }
	}
	
	private void printOutcomeHistogram() {
		int totalCases = 0;
		for (OutcomeType type: outcomeHist.keySet()) {
			totalCases += outcomeHist.get(type);
		}
		
		Map<OutcomeType,String> histPercent = new HashMap<OutcomeType,String>();
		for (OutcomeType type: outcomeHist.keySet()) {
			double percent = 100.0 * outcomeHist.get(type) / totalCases;
			String percentStr = new DecimalFormat("#.#").format(percent)+"%";
			histPercent.put(type, percentStr);
		}
		
		echo("\nCases with:");
		{		
			echo("  First decomposition is correct            : "+
					outcomeHist.get(OutcomeType.SUCCESS)+" ("+
				 	histPercent.get(OutcomeType.SUCCESS)+")");
			echo("  Corr. decomp. not in 1st place            : "+
					outcomeHist.get(OutcomeType.CORRECT_NOT_FIRST)+" ("+
				 	histPercent.get(OutcomeType.CORRECT_NOT_FIRST)+")");
			echo("  Some decomps produced but not correct one : "+
					outcomeHist.get(OutcomeType.CORRECT_NOT_PRESENT)+" ("+
				 	histPercent.get(OutcomeType.CORRECT_NOT_PRESENT)+")");
			echo("  No decomps produced at all                : "+
					outcomeHist.get(OutcomeType.NO_DECOMPS)+" ("+
				 	histPercent.get(OutcomeType.NO_DECOMPS)+")");		
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
		String correctDecomp = goldStandard.correctDecomp(word);
		OutcomeType gotOutcomeType = 
			expectations.type4outcome(gotOutcome, correctDecomp);
		
		incrementOutcomeHistogram(gotOutcomeType);
		
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

	private void incrementOutcomeHistogram(OutcomeType type) {
		Integer count = outcomeHist.get(type);
		outcomeHist.put(type, count+1);		
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
			if (caseData.isMisspelled || caseData.properName || 
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
		} catch (TimeoutException e) {
			outcome.timedOut = true;
		}
		
		return outcome;
	}

	private void echo(String mess) {
		System.out.println(mess);
	}
}
