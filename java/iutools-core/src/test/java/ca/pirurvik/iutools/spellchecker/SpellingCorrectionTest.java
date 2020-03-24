package ca.pirurvik.iutools.spellchecker;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.testing.AssertString;

public class SpellingCorrectionTest {

	@Test
	public void test__SpellingCorrection__Serialization() throws Exception {
		SpellingCorrection origCorr = 
			new SpellingCorrection("inukut", 
					new String[] {"inuktut", "inuktitut"}, 
					true);
		
		origCorr.correctLead = "inuk";
		origCorr.correctTail = "tut";
;	}

}
