package ca.inuktitutcomputing.morph.failureanalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import ca.inuktitutcomputing.morph.failureanalysis.ProblematicNGram.SortBy;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class MorphFailureAnalyserAsserter 
				extends Asserter<MorphFailureAnalyzer> {
	

	public MorphFailureAnalyserAsserter(
			MorphFailureAnalyzer _gotObject, String mess) {
		super(_gotObject, mess);
	}

	MorphFailureAnalyzer analyzer() {
		return gotObject;
	}

	public MorphFailureAnalyserAsserter mostProblematicNgramEqual(SortBy sortBy, 
			String[] expTopNgrams) throws IOException {
		List<ProblematicNGram> gotProblems = analyzer().getProblems(sortBy);
		List<String> gotTopNgrams = new ArrayList<String>();
		for (int ii=0; ii < expTopNgrams.length; ii++) {
			if (ii > gotProblems.size() - 1) {
				break;
			}
			gotTopNgrams.add(gotProblems.get(ii).ngram);
		}
		
		AssertObject.assertDeepEquals(
				baseMessage+"\nTop ngrams were not as expected (sorted by: "+
					sortBy.name()+")", 
				expTopNgrams, gotTopNgrams);
		
		return this;
	}

	public MorphFailureAnalyserAsserter statsForNgramEqual(String ngram, 
			double expFSRaio, int expNumFail, Long expFailureMass) {
		return statsForNgramEqual(ngram, 
				expFSRaio, expNumFail, expFailureMass,
				null, null);
	}
	
	public MorphFailureAnalyserAsserter statsForNgramEqual(String ngram, 
			Double expFSRatio, int expNumFail, Long expFailureMass,
			String[] expFailures, String[] expSuccesse) {
		
		Double exp = null;
		Double got = null;
		AssertNumber.assertEquals("Nevermind ",exp, got, 0.01);
		
		if (expFailureMass == null) {
			expFailureMass = new Long(-1);
		}
		
		if (expFailures == null) {
			expFailures = new String[0];
		}
		
		if (expSuccesse == null) {
			expSuccesse = new String[0];
		}
		
		ProblematicNGram ngramStats = analyzer().statsForNGram(ngram);
		
		Double gotFSRatio = ngramStats.getFailSucceedRatio();
		AssertNumber.assertEquals(
				baseMessage+"\nFail/Success ratio not as expected for ngram "+
					ngram, 
					expFSRatio, gotFSRatio, 0.01);
		
		long gotNumFail = ngramStats.getNumFailures();
		Assert.assertEquals(
				baseMessage+"\nNumber of failing words not as expected for ngram "+
					ngram, 
					expNumFail, gotNumFail);

		Long gotFailureMass = new Long(ngramStats.failureMass);
		Assert.assertEquals(
				baseMessage+"\nFailure mass not as expected for ngram "+
					ngram, 
					expFailureMass, gotFailureMass);
		
		for (String expExample: expFailures) {
			String mess = 
				baseMessage+
				"\nExamples of failure for ngram "+ngram+
				" were not as expected.\nShould have contained word: "+
				expExample+"\nGot examples: "+
				StringUtils.join(ngramStats.failureExamples().iterator(), ", ");
			
			Assert.assertTrue(mess, 
				ngramStats.failureExamples().contains(expExample));
		}

		for (String expExample: expSuccesse) {
			String mess = 
				baseMessage+
				"\nExamples of successes for ngram "+ngram+
				" were not as expected.\nShould have contained word: "+
				expExample+"\nGot examples: "+
				StringUtils.join(ngramStats.successExamples().iterator(), ", ");
			
			Assert.assertTrue(mess, 
				ngramStats.successExamples().contains(expExample));
		}

		return this;
	}
}
