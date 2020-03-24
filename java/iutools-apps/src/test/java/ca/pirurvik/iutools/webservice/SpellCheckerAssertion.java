package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;

import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.spellchecker.SpellingCorrection;
import ca.pirurvik.iutools.testing.EndpointAssertion;
import ca.pirurvik.iutools.webservice.SpellResponse;

public class SpellCheckerAssertion extends EndpointAssertion {
	
	SpellResponse gotSpellResponse = null;
	List<SpellingCorrection> gotCorrection = null;
	
	public static SpellCheckerAssertion assertThat(
			MockHttpServletResponse response, String mess) throws IOException {
		
		return new SpellCheckerAssertion(response, mess);
	}
	
	public SpellCheckerAssertion(MockHttpServletResponse response,
				String mess) throws IOException {
		super(response, mess);
		gotSpellResponse = IUTServiceTestHelpers.toSpellResponse(response);
		gotCorrection = gotSpellResponse.correction;
	}

	public SpellCheckerAssertion raisedNoError() {
		Assert.assertTrue(baseMessage+"\nThe response raised errors", 
				gotSpellResponse.errorMessage == null);
		return this;
	}

	public SpellCheckerAssertion nthCorrectionIs(int nn, 
			Boolean expMisspelled) throws Exception {
		return nthCorrectionIs(nn, expMisspelled, null);
	}
	
	public SpellCheckerAssertion nthCorrectionIs(int nn, 
			Boolean expMisspelled, String[] expSuggestions) 
			throws Exception {
		
		SpellingCorrection nthCorrection = gotCorrection.get(nn);
		
		String wordDescr = "word #"+nn+"='"+nthCorrection.orig+"'";
		
		Assert.assertEquals(
				baseMessage+"\nMisspelled status was wrong for "+wordDescr, 
				expMisspelled, nthCorrection.wasMispelled);
		
		if (nthCorrection.wasMispelled) {
			AssertObject.assertDeepEquals(
					baseMessage+"\nSuggestions were wrong for"+wordDescr, 
					expSuggestions, nthCorrection.getPossibleSpellings());
		}
		
		return this;
	}

}
