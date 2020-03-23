package ca.pirurvik.iutools.spellchecker;

import java.util.List;

import org.junit.Assert;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;

public class AssertSpellingCorrection {

	private String baseMessage;
	private SpellingCorrection gotCorrection;

	public AssertSpellingCorrection(SpellingCorrection _gotCorrection, 
				String _mess) {
		this.baseMessage = _mess;
		this.gotCorrection = _gotCorrection;
	}
	
	public AssertSpellingCorrection wasMisspelled() {
		Assert.assertTrue(baseMessage+"\nWord should have been mis-spelled", 
				gotCorrection.wasMispelled);
		return this;
	}

	public AssertSpellingCorrection wasNotMisspelled() {
		Assert.assertFalse(baseMessage+"\nWord should NOT have been mis-spelled", 
				gotCorrection.wasMispelled);
		return this;
	}

	public AssertSpellingCorrection suggestsSpellings(String[] expSuggs) 
				throws Exception {
		List<String> gotSuggs = gotCorrection.getPossibleSpellings();
		AssertObject.assertDeepEquals(
				baseMessage+"\nSuggested spellings were not as expected.", 
				expSuggs, gotSuggs);
		return this;
	}

	public AssertSpellingCorrection suggestsCorrectLead(String expLead) {
		String gotLead = gotCorrection.correctLead;
		AssertString.assertStringEquals(
			baseMessage+"\nSuggested correct leading chars were not as expected.",
			expLead, gotLead);
		return this;
	}

	public AssertSpellingCorrection suggestsCorrectTail(String expTail) {
		String gotTail = gotCorrection.correctTail;
		AssertString.assertStringEquals(
			baseMessage+"\nSuggested correct tailing chars were not as expected.",
			expTail, gotTail);
		return this;
	}

	public AssertSpellingCorrection suggestsCorrectExtremities(String expPartial) {
		String gotPartial = gotCorrection.correctExtremities();
		AssertString.assertStringEquals(
			baseMessage+"\nSuggested correct portions of the word were not as expected.",
			expPartial, gotPartial);
		return this;
	}

}
