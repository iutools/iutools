package ca.pirurvik.iutools.spellchecker;

import java.util.List;

import org.junit.Assert;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;

public class SpellingCorrectionAsserter {

	private String baseMessage;
	private SpellingCorrection gotCorrection;
	
	public static SpellingCorrectionAsserter assertThat(
			SpellingCorrection _gotCorrection, 
			String _mess) {
		
		return new SpellingCorrectionAsserter(_gotCorrection, _mess);
	}

	public SpellingCorrectionAsserter(SpellingCorrection _gotCorrection, 
				String _mess) {
		this.baseMessage = _mess;
		this.gotCorrection = _gotCorrection;
	}
	
	public SpellingCorrectionAsserter wasMisspelled() {
		Assert.assertTrue(baseMessage+"\nWord should have been mis-spelled", 
				gotCorrection.wasMispelled);
		return this;
	}

	public SpellingCorrectionAsserter wasNotMisspelled() {
		Assert.assertFalse(baseMessage+"\nWord should NOT have been mis-spelled", 
				gotCorrection.wasMispelled);
		return this;
	}

	public SpellingCorrectionAsserter suggestsSpellings(String[] expSuggs) 
				throws Exception {
		List<String> gotSuggs = gotCorrection.getPossibleSpellings();
		AssertObject.assertDeepEquals(
				baseMessage+"\nSuggested spellings were not as expected.", 
				expSuggs, gotSuggs);
		return this;
	}

	public SpellingCorrectionAsserter highlightsIncorrectTail(String expLead) {
		String gotLead = gotCorrection.getCorrectLead();
		AssertString.assertStringEquals(
			baseMessage+"\nSuggested correct leading chars were not as expected.",
			expLead, gotLead);
		return this;
	}

	public SpellingCorrectionAsserter highlightsIncorrectLead(String expTail) {
		String gotTail = gotCorrection.getCorrectTail();
		AssertString.assertStringEquals(
			baseMessage+"\nSuggested correct tailing chars were not as expected.",
			expTail, gotTail);
		return this;
	}

	public SpellingCorrectionAsserter highlightsIncorrectMiddle(String expPartial) {
		String gotPartial = gotCorrection.highlightIncorrectMiddle();
		AssertString.assertStringEquals(
			baseMessage+"\nSuggested correct portions of the word were not as expected.",
			expPartial, gotPartial);
		return this;
	}

	public SpellingCorrectionAsserter providesSuggestions(String[] expSugg) 
				throws Exception {
		AssertObject.assertDeepEquals(
				baseMessage+"\nSuggestions were not as expected for word "+
				gotCorrection.orig, 
				expSugg, gotCorrection.getAllSuggestions());
		return this;
	}
}
