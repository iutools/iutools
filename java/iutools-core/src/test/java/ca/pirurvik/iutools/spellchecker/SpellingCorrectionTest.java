package ca.pirurvik.iutools.spellchecker;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.testing.AssertString;

public class SpellingCorrectionTest {
	
	//////////////////////
	// VERFICATION TESTS
	//////////////////////

	@Test
	public void test__highlightIncorrectMiddle__LeadAndTailLeaveGap() {
		SpellingCorrection gotCorr = 
			new SpellingCorrection()
				.setOrig("inuqtitut")
				.setCorrectLead("inu")
				.setCorrectTail("tut")
			;
		
		SpellingCorrectionAsserter.assertThat(
			gotCorr, 
			"There should be no middle highlighting when tail and lead exactly cover the word")
				.highlightsIncorrectMiddle("inu[qti]tut")
		;
	}

	@Test
	public void test__highlightIncorrectMiddle__LeadAndTailOverlap() {
		SpellingCorrection gotCorr = 
			new SpellingCorrection()
				.setOrig("inuqtitut")
				.setCorrectLead("inuqti")
				.setCorrectTail("qtitut")
			;
		
		SpellingCorrectionAsserter.assertThat(
			gotCorr, 
			"There should be no middle highlighting when tail and lead exactly cover the word")
				.highlightsIncorrectMiddle("inu[qti]tut")
		;
	}
	
	@Test
	public void test__highlightIncorrectMiddle__LeadAndTailExactlyCoverWord() {
		SpellingCorrection gotCorr = 
			new SpellingCorrection()
				.setOrig("inuqtitut")
				.setCorrectLead("inuq")
				.setCorrectTail("titut")
			;
		
		SpellingCorrectionAsserter.assertThat(
			gotCorr, 
			"There should be no middle highlighting when tail and lead exactly cover the word")
				.highlightsIncorrectMiddle(null)
		;
	}
	
	
	@Test
	public void test__SpellingCorrection__Serialization() throws Exception {
		SpellingCorrection origCorr = 
			new SpellingCorrection("inukut", 
					new String[] {"inuktut", "inuktitut"}, 
					true);
		
		origCorr.correctLead = "inuk";
		origCorr.correctTail = "tut";
	}
}
