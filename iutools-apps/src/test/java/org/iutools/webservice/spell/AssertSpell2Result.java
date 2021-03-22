package org.iutools.webservice.spell;

import ca.nrc.testing.AssertObject;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.junit.jupiter.api.Assertions;

public class AssertSpell2Result extends AssertEndpointResult {
	public AssertSpell2Result(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertSpell2Result(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}
	
	public AssertSpell2Result nthCorrectionIs(int nn, 
			Boolean expMisspelled) throws Exception {
		return nthCorrectionIs(nn, expMisspelled, null);
	}
	
	public AssertSpell2Result nthCorrectionIs(int nn, 
			Boolean expMisspelled, String[] expSuggestions) 
			throws Exception {
		
		SpellingCorrection nthCorrection = result().correction.get(nn);
		
		String wordDescr = "word #"+nn+"='"+nthCorrection.orig+"'";
		
		Assertions.assertEquals(
			expMisspelled, nthCorrection.wasMispelled,
			baseMessage+"\nMisspelled status was wrong for "+wordDescr);
		
		if (nthCorrection.wasMispelled) {
			AssertObject.assertDeepEquals(
				baseMessage+"\nSuggestions were wrong for"+wordDescr,
				expSuggestions, nthCorrection.getPossibleSpellings());
		}
		
		return this;
	}
	
	Spell2Result result() {
		return (Spell2Result)gotObject;
	}
}