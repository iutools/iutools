package org.iutools.webservice.spell;

import ca.nrc.testing.AssertObject;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.search.ExpandQueryResult;
import org.junit.jupiter.api.Assertions;

public class AssertSpellResult extends AssertEndpointResult {

	@Override
	protected SpellResult result() {
		return (SpellResult)gotObject;
	}

	public AssertSpellResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertSpellResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}
	
	public AssertSpellResult nthCorrectionIs(int nn,
														  Boolean expMisspelled) throws Exception {
		return nthCorrectionIs(nn, expMisspelled, null);
	}
	
	public AssertSpellResult nthCorrectionIs(int nn,
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
}