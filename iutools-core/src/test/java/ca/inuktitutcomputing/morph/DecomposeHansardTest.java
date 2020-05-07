/*
 * Created on Aug 19, 2004
 *
 * 
 */
package ca.inuktitutcomputing.morph;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeoutException;

import ca.inuktitutcomputing.data.LinguisticData;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphAnalCurrentExpectations.OutcomeType;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;

/**
 * @author Marta
 *
 */
public class DecomposeHansardTest {
	
	boolean verbose = true;
	
	MorphologicalAnalyzer morphAnalyzer = null;
	
	/*
	 * @see TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		morphAnalyzer = new MorphologicalAnalyzer();
		LinguisticData.init();		
	}
	
	@Test
	public void testDecomposer() throws Exception {
				
		System.out.println("Running testDecomposer. This test can take a few minutes to complete.");
		
		//
		// Leave this at null to run on all words
		// Set it to a word if you want to run the tests just on that one word.
		//
		String focusOnWord = null;
//		focusOnWord = "iksivautaaq";
		
		morphAnalyzer = new MorphologicalAnalyzer();
		MorphAnalCurrentExpectations expectations = new MorphAnalCurrentExpectations();
		
		Calendar startCalendar = Calendar.getInstance();
		
		MorphAnalGoldStandard goldStandard = new MorphAnalGoldStandard();
        Map<String,String> outcomeDifferences = new HashMap<String,String>();
        
		for (String wordToBeAnalyzed: goldStandard.allWords()) {
		    boolean noProcessing = false;
		    AnalyzerCase caseData = goldStandard.caseData(wordToBeAnalyzed);

		    if (skipCase(caseData, focusOnWord)) {
		    	continue;
		    }

		    String goldStandardDecomposition = caseData.correctDecomp;
			if (verbose) System.out.print("> :"+wordToBeAnalyzed+":");
		    AnalysisOutcome outcome = decompose(wordToBeAnalyzed);
		    
			Decomposition [] decs = outcome.decompositions;
            if (verbose) System.out.println(" []");
            
            checkOutcome(wordToBeAnalyzed, outcome, expectations, goldStandard, 
            		outcomeDifferences);
		}
		
		Calendar endCalendar = Calendar.getInstance();
		
		long time = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
		
        if (verbose) System.out.println("");
        if (verbose) System.out.println("Time in milliseconds: "+time);
        
        assertOutcomesHaveNotChanged(outcomeDifferences);
        
        if (focusOnWord != null) {
        	Assert.fail("Test was only run on single word "+focusOnWord+
        		"\nDon't forget to reset focusOnWord=null before committing!");
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

		OutcomeType expOutcome = expectations.expectedOutcome(word);
		String correctDecomp = goldStandard.correctDecomp(word);
		
		if (expOutcome == OutcomeType.SUCCESS) {
			checkOutcomeAgainstSuccess(word, gotOutcome, correctDecomp, outcomeDiffs);
		} else if (expOutcome == OutcomeType.CORRECT_NOT_FIRST) {
			checkOutcomeAgainstCorrectNotFirst(word, gotOutcome, correctDecomp, outcomeDiffs);			
		} else if (expOutcome == OutcomeType.CORRECT_NOT_PRESENT) {
			checkOutcomeAgainstCorrectNotPresent(word, gotOutcome, correctDecomp, outcomeDiffs);			
		} else if (expOutcome == OutcomeType.NO_DECOMPS) {
			checkOutcomeAgainstNoDecomps(word, gotOutcome, outcomeDiffs);
		} else if (expOutcome == OutcomeType.TIMEOUT) {
			checkOutcomeAgainstTimeout(word, gotOutcome, outcomeDiffs);
		}
	}
	
	private void checkOutcomeAgainstSuccess(String word, 
			AnalysisOutcome gotOutcome, String correctDecomp, 
			Map<String, String> outcomeDiffs) {
		String gotTopDecomp = null;
		Decomposition[] gotDecomps = gotOutcome.decompositions;
		if (gotDecomps != null && gotDecomps.length > 0) {
			gotTopDecomp = gotDecomps[0].toString();
		}
		if (!correctDecomp.equals(gotTopDecomp)) {
			// Top decomp used to be the correct one but it isn't anymore
			String diffMess = 
				"Top decomposition used to be correct but it isn't anymore.\n"+
			    "\n"+
				"Correct decomp    : "+correctDecomp+"\n"+
			    "Top decomp is now : "+gotTopDecomp;
			logOutcomeDifference(word, diffMess, false, outcomeDiffs);
		}
	}	

	private void checkOutcomeAgainstCorrectNotFirst(String word, 
		AnalysisOutcome gotOutcome, String correctDecomp, 
		Map<String, String> outcomeDiffs) {
		
		if (!gotOutcome.includesDecomp(correctDecomp)) {
			String diffMess = 
				"Decompositions for the word used to include the correct one, but they don't anymore.\n"+
				"Correct decomp : "+correctDecomp+"\n"+
				"Got decomps:\n"+gotOutcome.joinDecomps();
			logOutcomeDifference(word, diffMess, false, outcomeDiffs);
		}		
	}

	private void checkOutcomeAgainstCorrectNotPresent(String word, 
			AnalysisOutcome gotOutcome, String correctDecomp,
			Map<String, String> outcomeDiffs) {
		
		if (gotOutcome.includesDecomp(correctDecomp)) {
			String diffMess =
				"Correct decomposition did NOT use to be in the list of decompositions, but it now is!";
			logOutcomeDifference(word, diffMess, true, outcomeDiffs);
		}		
	}
	
	private void checkOutcomeAgainstNoDecomps(String word, AnalysisOutcome gotOutcome, Map<String, String> outcomeDiffs) {
		Decomposition[] gotDecomps = gotOutcome.decompositions;
		if (gotDecomps != null && gotDecomps.length > 0) {
			String diffMess = 
				"Word is now producing decompositions!\n";
			logOutcomeDifference(word, diffMess, true, outcomeDiffs);
		}
	}

	private void checkOutcomeAgainstTimeout(String word, 
			AnalysisOutcome gotOutcome, Map<String, String> outcomeDiffs) {
		if (!gotOutcome.timedOut) {
			logOutcomeDifference(word, "Word stopped timing out!", true,
					outcomeDiffs);
		}
	}
	
	private void logOutcomeDifference(String word, String diffMess, 
			boolean isImprovement, Map<String, String> outcomeDifferences) {
		
		if (isImprovement) {
			diffMess = "GOOD news!!!\n" + diffMess;
		} else {
			diffMess = "BAD news.\n" + diffMess;
		}
		outcomeDifferences.put(word, diffMess);
	}

	private boolean skipCase(AnalyzerCase caseData, String focusOnWord) {
		Boolean skip = null;
		
		if (focusOnWord != null  && !focusOnWord.equals(caseData.word)) {
			skip = true;
		}
		
		if (skip == null) {
			if (caseData.isMisspelled || caseData.properName || 
				 caseData.skipped || caseData.decompUnknown) {
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
